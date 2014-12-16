# -*- coding: utf-8 -*-
'''
Created on 28 nov. 2013

@author: grau
'''

from libCommun import *
import os, subprocess, time
from scipy import stats, interpolate
import matplotlib as mpl
from matplotlib.lines import Line2D

listeMethodes = ["BL2", "BL1", "BL3", "BL4", "Surf", "volume", "test"]
nbTirsTheorique = ((360 * 180) / (0.05 * 0.05)) / (4 * np.pi)


class Maille:

  def __init__(self):
    self.listeVoxels = {}
    self.nbX = 0
    self.nbY = 0
    self.nbZ = 0
    self.type = "classic"  # maillage calcule par voxelisation (par defaut : on utilise alors la formule pour calculer la densite) ou par DART : on renvoit directement la densite stockee dans le fichier
    self.modeCalculDensite = listeMethodes[0]
    self.nomFichier = ""

  def keys(self): return self.listeVoxels.keys()
  def items(self): return self.listeVoxels.items()
  def values(self): return self.listeVoxels.values()
  def __getitem__(self, key):
#    if key in self.listeVoxels:
      return self.listeVoxels[key]
#    else:
#      return np.nan
  def __len__(self):  return len(self.listeVoxels)
  def __setitem__(self, key, item): self.listeVoxels[key] = item

  def initialiser(self, nbX, nbY, nbZ):
    self.nbX = nbX
    self.nbY = nbY
    self.nbZ = nbZ

  def getDensite(self, indice, modeCalculDensite = -1):
    if modeCalculDensite == -1:
      modeCalculDensite = self.modeCalculDensite
    if not indice in self.listeVoxels: return np.nan

    if self.type == "classic":
      return self.listeVoxels[indice].getDensite(modeCalculDensite)
    else:
      return self.listeVoxels[indice].getDensiteStockee()

  def getTransmittance(self, indice):
    if not indice in self.listeVoxels: return np.nan
    if self.listeVoxels[indice].getNbTotal() < Voxel.seuilCalculDensite : return np.nan
    if self.type == "classic":
      return self.listeVoxels[indice].getTransmittance(self.modeCalculDensite)
    else:
      return np.exp(-self.listeVoxels[indice].getDensiteStockee() * self.listeVoxels[indice].getG() * Voxel.resolution)

  def getNbTotal(self, indice):
    if not indice in self.listeVoxels: return np.nan
    return self.listeVoxels[indice].getNbTotal()

  def getNbDedans(self, indice):
    if not indice in self.listeVoxels: return np.nan
    return self.listeVoxels[indice].nbDedans

  def getNbApres(self, indice):
    if not indice in self.listeVoxels: return np.nan
    return self.listeVoxels[indice].nbApres

  def getNbVoxels(self):
    return self.nbX * self.nbY * self.nbZ


  def supprimeVoxelsCoucheZ(self, k):
    for i in range(self.nbX):
      for j in range(self.nbY):
        indice = (i, j, k)
        self.listeVoxels[indice].RAZ()

  def supprimeVoxelsDerniereCoucheZ(self):
    for i in range(self.nbX):
      for j in range(self.nbY):
        indice = (i, j, self.nbZ - 1)
        del self.listeVoxels[indice]
        # self.listeVoxels[indice].RAZ()

  def supprimeVoxel(self, indice):
    if indice in  self.listeVoxels:
      del self.listeVoxels[(i, j, k)]
      # self.listeVoxels[indice].RAZ()

  def supprimeVoxel_ijk(self, i, j, k):
    if (i, j, k) in  self.listeVoxels:
      del self.listeVoxels[(i, j, k)]
      # self.listeVoxels[(i, j, k)].RAZ()

  def supprimeVoxelsCoucheXY(self, limiteY = 0):
    for i in range(self.nbX):
      for j in range(self.nbY):
        for k in range(self.nbZ):
          if j > limiteY / Voxel.resolution:
            indice = (i, j, k)
            self.listeVoxels[indice].RAZ()

  def fusionnerVoxel(self, voxelCourant):
    if voxelCourant.indice in  self.listeVoxels:
      self.listeVoxels[voxelCourant.indice].longueur += voxelCourant.longueur
      self.listeVoxels[voxelCourant.indice].longueurIntercept += voxelCourant.longueurIntercept
      self.listeVoxels[voxelCourant.indice].nbApres += voxelCourant.nbApres
      self.listeVoxels[voxelCourant.indice].nbDedans += voxelCourant.nbDedans
#      self.listeVoxels[voxelCourant.indice].surfaceVue  += voxelCourant.surfaceVue
      if voxelCourant.densite > 0:
        self.listeVoxels[voxelCourant.indice].densite += voxelCourant.densite
    else: print "voxels non cohérents avec les voxels à fusionner"

  def fusionnerVoxelPondere(self, voxelCourant):
    if voxelCourant.indice in  self.listeVoxels:
#      a = self.listeVoxels[voxelCourant.indice].getSurfaceFaisceauEntrant() / self.listeVoxels[voxelCourant.indice].getAngleSolide()
#      b = voxelCourant.getSurfaceFaisceauEntrant() / voxelCourant.getAngleSolide()
      a = self.listeVoxels[voxelCourant.indice].getSurfaceFaisceauEntrant()
      b = voxelCourant.getSurfaceFaisceauEntrant()

#      print a, b, "dedans", self.listeVoxels[voxelCourant.indice].nbDedans, voxelCourant.nbDedans , "apres", self.listeVoxels[voxelCourant.indice].nbApres, voxelCourant.nbApres
#      print "apres: ", self.listeVoxels[voxelCourant.indice].nbApres * a + voxelCourant.nbApres * b, "dedans",  (self.listeVoxels[voxelCourant.indice].nbDedans * a + voxelCourant.nbDedans * b)
      self.listeVoxels[voxelCourant.indice].longueur = (self.listeVoxels[voxelCourant.indice].longueur * a + voxelCourant.longueur * b) / (a + b)
      self.listeVoxels[voxelCourant.indice].longueurIntercept = (self.listeVoxels[voxelCourant.indice].longueurIntercept * a + voxelCourant.longueurIntercept * b) / (a + b)
      self.listeVoxels[voxelCourant.indice].nbApres = (self.listeVoxels[voxelCourant.indice].nbApres * a + voxelCourant.nbApres * b) / (a + b)
      self.listeVoxels[voxelCourant.indice].nbDedans = (self.listeVoxels[voxelCourant.indice].nbDedans * a + voxelCourant.nbDedans * b) / (a + b)
#      self.listeVoxels[voxelCourant.indice].surfaceVue += voxelCourant.surfaceVue
#      print "dedans /(dedans+entrant" , self.listeVoxels[voxelCourant.indice].nbDedans / (self.listeVoxels[voxelCourant.indice].nbDedans + self.listeVoxels[voxelCourant.indice].nbApres)
      if voxelCourant.densite > 0:
        self.listeVoxels[voxelCourant.indice].densite += voxelCourant.densite
    else: print "voxels non cohérents avec les voxels à fusionner"

  def ajouterVoxel(self, voxelCourant):
    self.listeVoxels[voxelCourant.indice] = voxelCourant
    if voxelCourant.indice[0] > self.nbX:
      self.nbX = voxelCourant.indice[0]
    if voxelCourant.indice[1] > self.nbY:
      self.nbY = voxelCourant.indice[0]
    if voxelCourant.indice[2] > self.nbZ:
      self.nbZ = voxelCourant.indice[0]

  def ajouterFonctionG(self, cheminFichierG):
    # print "foooncitiongggg"
    if os.path.isfile(cheminFichierG):
      fic = open(cheminFichierG, "r")
      valeurG = {}
      sommeG = 0
      nbG = 0
      for k in range(self.nbZ):
        for i in range(self.nbX):
          ligne = fic.readline()
          lig = map(float, ligne.rstrip('\n\r').split())
          for j in range(self.nbY):
            self.listeVoxels[(i, j, k)].G = lig[j]
            if lig[j] > 0 :
              sommeG += lig[j]
              nbG += 1

        ligne = fic.readline()
      # print "Moyenne G:", sommeG / nbG
    else:
      print "pas de fichier G trouve", cheminFichierG
      # exit()


  def getListeDensites(self, modeCalculDensite):
    listeRho = []
    for indice in self.listeVoxels.keys():
      rho = self.getDensite(indice, modeCalculDensite)
      if ~np.isnan(rho) and rho > 0:
        listeRho.append(rho)
        # print "ldenn", indice, rho
    return listeRho

  def getListeDensitesNonFiltre(self, modeCalculDensite):
    listeRho = []
    for indice in self.listeVoxels.keys():
      rho = self.getDensite(indice, modeCalculDensite)
      if ~np.isnan(rho) and rho > 0:
        listeRho.append(rho)
      else:
        listeRho.append(0)
        # print "ldenn", indice, rho
    return listeRho


  def getListeAttributsLongueur(self, listeIndices):
    listeG, listeTotal, listeLg, listeIntercept, listeParcourt, listeRho = [], [], [], [], [], [],
    for indice in listeIndices:
      rho = self.listeVoxels[indice].getDensite()
      listeRho.append(rho)
      listeTotal.append(self.listeVoxels[indice].getNbTotal())
      listeLg.append(self.listeVoxels[indice].getLongueurMoyenne())
      listeParcourt.append(self.listeVoxels[indice].getLongueurParcourtMoyen())
      listeIntercept.append(self.listeVoxels[indice].getLongueurAvantInterceptMoyen())
      listeG.append(self.listeVoxels[indice].getG())
    return listeG, listeRho, listeTotal, listeLg, listeParcourt, listeIntercept

  def getListeLgMoyenne(self):
    liste = []
    for vox in self.listeVoxels.values():
      lg = vox.getLongueurParcourtMoyen()
      if not np.isnan(lg):
        liste.append(vox.getLongueurMoyenne())
    return liste

  def getListeLgParcourtMoyen(self, listeIndices):
    liste = []
    for vox in self.listeVoxels.values():
      lg = vox.getLongueurParcourtMoyen()
      if not np.isnan(lg):
        liste.append(vox.getLongueurParcourtMoyen())
    return liste

  def calculeMNT(self, cheminImage = "", cheminFichier = ""):
    X, Y, Z = [], [], []
    for i in range(self.nbX):
      for j in range(self.nbY):
        colonneOK = False
        for k in range(self.nbZ):
          indice = (i, j, k)
          # print indice, self.listeVoxels[indice].nbDedans
          if self.listeVoxels[indice].nbDedans > 0:
            X.append(i)
            Y.append(j)
            Z.append(k)
            colonneOK = True
            break
        if not colonneOK:
          X.append(i)
          Y.append(j)
          Z.append(np.nan)

    xi, yi = np.mgrid[0:self.nbX, 0:self.nbY]
#    zi = interpolate.griddata((X, Y), np.array(Z), (xi, yi), method = 'linear')
#
#    plt.imshow(zi)
#    plt.show()

    print np.shape(Z), self.nbX, self.nbY
    znew = np.reshape(Z, (self.nbX, self.nbY))

#    plt.imshow(znew)
#    plt.show()

    def test_func(values):
      idxCentre = int(len(values) / 2 + 0.5)
#      print np.shape(values), values, idxCentre
#      print values[idxCentre]
      val = values[idxCentre]
      autour = [v for i, v in enumerate(values) if i != idxCentre and not np.isnan(v)]

#      print np.shape(values), values, val, autour, np.mean(autour)
      if autour:
        if np.isnan(val):
          return np.min(autour)
        if np.abs(val - np.min(autour)) > 5:
          return np.min(autour)
      return val

#    footprint = np.array([[1,1,1],
#                          [1,1,1],
#                          [1,1,1]])

    import scipy.ndimage as ndimage
    zi = ndimage.generic_filter(znew, test_func, size = 15)
    zi = ndimage.generic_filter(zi, test_func, size = 7)
    zi = ndimage.generic_filter(zi, test_func, size = 7)
    zi = ndimage.generic_filter(zi, test_func, size = 7)
    zi = ndimage.generic_filter(zi, test_func, size = 7)
#    zi = ndimage.generic_filter(zi, test_func, size = 3)
#    zi = ndimage.generic_filter(zi, test_func, size=4)
#    zi = ndimage.median_filter(zi, 3)

    plt.imshow(zi * Voxel.resolution, origin = 'lower' , aspect = 'equal')
    plt.colorbar()

    if cheminFichier:
      print "ecriture MNT : ", cheminFichier
      fic = open(cheminFichier, "w")
      fic.write("%d %d %f\n" % (self.nbX, self.nbY, Voxel.resolution))
      xi, yi = np.mgrid[0:self.nbX, 0:self.nbY]
      for i in range(self.nbX):
        for j in range(self.nbY):
          if not np.isnan(zi[i][j]):
            fic.write("%f %f %f\n" % ((i + 0.5) * Voxel.resolution, (j + 0.5) * Voxel.resolution, zi[i][j] * Voxel.resolution))

    if cheminImage:
      plt.imsave(cheminImage, zi)  # uses the Image module (PIL)
    else:
      plt.show()

    return zi




  def afficheProjectionZ(self, cheminImage = ""):
#    import pylab
    image = np.zeros((self.nbX, self.nbY))
    for i in range(self.nbX):
      for j in range(self.nbY):
        for k in range(self.nbZ):
            indice = (i, j, k)
            if indice in self.listeVoxels:
              rho = self.listeVoxels[indice].getDensite()
              if not np.isnan(rho):
                image [i, j] += rho * Voxel.resolution ** 3

#    fig = pylab.figure(frameon=False)
#    ax_size = [0,0,1,1]
#    fig.add_axes(ax_size)
#    pylab.imshow(image, interpolation='nearest',  origin='lower')
#    pylab.axis('off')
#    pylab.savefig("/homeL/grau/test.png",bbox_inches='tight', pad_inches=0)

    if cheminImage:
      fig = plt.figure(frameon = False)
      ax_size = [0, 0, 1, 1]
      fig.add_axes(ax_size)
      plt.imshow(image, interpolation = "nearest", origin = 'upper')
      plt.axis('off')
      plt.savefig(cheminImage)
      plt.close()

    plt.Figure()
    plt.imshow(image, interpolation = "nearest")
    plt.colorbar()
    plt.show()
    plt.close()

  def afficheProfilLAI(self, cheminImage = ""):
    profil_rho, profil_T = self.getProfilLAI()
    plt.Figure()
    plt.title("LAI - total : " + str(round(np.sum(profil_rho),2)) + " $m^2/m^2$ ; Surface: " + str(round(  self.nbX * self.nbY * Voxel.resolution ** 2,2)) +" $m^2$")
    z = np.arange(0, (self.nbZ) * Voxel.resolution, Voxel.resolution)
    plt.plot(profil_rho, z)
    plt.ylabel('z [m]')
    plt.xlabel('LAI [$m^2/m^2$]')
    plt.tight_layout()
    if cheminImage:
      plt.savefig(cheminImage)
    else: plt.show()
    plt.close()

  def afficheDistributionDensite(self, cheminImage = ""):
    listeRho = self.getListeDensiteNonNanNonNul(self.getListeIndices())
    plt.Figure()
    if listeRho:
      plt.hist(listeRho, 100, histtype = 'bar')  # ,
                          #    color=['crimson', 'burlywood', 'chartreuse'],
                          #    label=['Crimson', 'Burlywood', 'Chartreuse'])
      plt.tight_layout()
      if cheminImage:
        plt.savefig(cheminImage)
      else: plt.show()
      plt.close()
    else:
      print "pas de densite trouvée pour fichier :", self.nomFichier

  def getProfilLAI(self):
    profil_lai, profil_T = np.zeros(self.nbZ), np.zeros(self.nbZ)
    nbCell_rho, nbCell_T = np.zeros(self.nbZ), np.zeros(self.nbZ)
    for k in range(self.nbZ):
      for i in range(self.nbX):
        for j in range(self.nbY):
          indice = (i, j, k)
          if indice in self.listeVoxels:
            rho = self.getDensite(indice) * Voxel.resolution ** 3
            T = self.getTransmittance(indice)
          else:
#            print "voxel not found", indice
            T = np.nan
            rho = np.nan
          if ~np.isnan(rho):
            profil_lai[k] += rho
            nbCell_rho[k] += 1
          if ~np.isnan(T):
            profil_T[k] += T
            nbCell_T[k] += 1
#          if k == 3:
#            print i, j, k, "\t", T, rho
#      print k, profil_T[k], profil_rho[k]

    surface = self.nbX * self.nbY * Voxel.resolution ** 2
    for k in range(self.nbZ):
      profil_lai[k] /= surface
#      if nbCell_rho[k] > 0:
#        profil_rho[k] /= nbCell_rho[k]
#      if nbCell_T[k] > 0:
#        profil_T[k] /= nbCell_T[k]

    return profil_lai, profil_T

#    return profil_rho / (self.nbY * self.nbX * Voxel.resolution), profil_T / (nbCell_T[k])

  def ecrireFichierDensite(self, cheminFichier):
    fic = open(cheminFichier, "w")
    print "ecriture fichier ", cheminFichier
    fic.write("%d %d %d %f\n" % (self.nbX, self.nbY, self.nbZ, Voxel.resolution))
    for k in range(self.nbZ):
      for i in range(self.nbX):
        for j in range(self.nbY):
          indice = (i, j, k)
          if indice in self.listeVoxels:
            rho = self.getDensite(indice)
            if rho > 0 and ~np.isnan(rho):
              fic.write("%d %d %d %.6f %.6f %.6f %.6f %.6f %.6f %.6f\n" % (i, j, k, self.listeVoxels[indice].longueur, self.listeVoxels[indice].longueurIntercept, self.listeVoxels[indice].nbDedans, self.listeVoxels[indice].nbApres, self.listeVoxels[indice].surfaceVue, self.listeVoxels[indice].distanceAuScanner, rho))
            else:
              fic.write("%d %d %d %.6f %.6f %.6f %.6f %.6f %.6f %.1f\n" % (i, j, k, self.listeVoxels[indice].longueur, self.listeVoxels[indice].longueurIntercept, self.listeVoxels[indice].nbDedans, self.listeVoxels[indice].nbApres, self.listeVoxels[indice].surfaceVue, self.listeVoxels[indice].distanceAuScanner, -1))

    fic.close()


  def ecrireFichierMaket(self, cheminFichier):
    fic = open(cheminFichier, "w")
    R = Voxel.resolution
    fic.write("%f %f %f\n%f %f %f\n%d" % (self.nbX * R, self.nbY * R, self.nbZ * R, R, R, R, self.nbX * self.nbY))
    factor = R * R
    for k in range(self.nbZ):
      for i in range(self.nbX):
        for j in range(self.nbY):
          indice = (i, j, k)
          rho = self.getDensite(indice)
          if k == 0:
            if rho == 0 or np.isnan(rho):
              fic.write("2 1 0 0 0 ")
            else:
              fic.write("6 1 0 1 " + str(rho / factor) + " 0 0 ")
          else:
            if rho == 0 or np.isnan(rho):
              fic.write("0 ")
            else:
              fic.write("6 0 1 " + str(rho / factor) + " 0 0 ")
        fic.write("\n")
      fic.write("\n")
    fic.close()
    print "fichier", cheminFichier, "ecrit"


  def ajouterDensiteDansFichiersMaket(self, cheminFichier):
    print "ajout densite...",

    fic = open(cheminFichier, "r")
    x, y, z = fic.readline().rstrip('\n\r').split(" ")
    x, y, z = float(x), float(y), float (z)
    _nbx, _nby, _nbz = fic.readline().rstrip('\n\r').split(" ")
    nbx, nby, nbz = int(x / float(_nbx)), int(y / float(_nby)), int (z / float (_nbz))

    nbCell = fic.readline().rstrip('\n\r').split(" ")

    typeCellule = {}
    densiteParCellule = {}
    nbTriangles, numTrianglesParCellule = {}, {}

    if self.nbX != nbx or self.nbY != nby:
      print "erreur : nombre de cellules differents dans fichier : ", maquette
      return

    # lecture du fichier et stockage de la densite : une ligne correspond a toutes les cellules suivant y
    for k in range (nbz):
      for i in range (nbx):
        ligne = fic.readline().rstrip('\n\r').split(" ")
#        print ligne
        id = 0
        for j in range (nby):
          indice = (i, j, k)
          typeCellule[indice] = int(ligne[j + id])
#          print indice, "type ", typeCellule[indice],
          if typeCellule[indice] != 0:
            id += 1
            nbTriangles[indice] = int(ligne[j + id])
#            print "nbTri ", nbTriangles[indice]
            listeTriangles = []
            for numTri in range(nbTriangles[indice]):
              id += 1
              listeTriangles.append(int(ligne[j + id]))
#              print "num Tri ", int(ligne[j + id])

            if listeTriangles: numTrianglesParCellule[indice] = listeTriangles
            id += 1
            nbDensite = int(ligne[j + id])
            densite = 0
            for numTri in range(nbDensite):
              id += 1
              densite += float(ligne[j + id])
              id += 1
            densiteParCellule[indice] = densite
            id += 1
          else:
            nbTriangles[indice] = 0
            densiteParCellule[indice] = 0

      ligne = fic.readline()  # saut de ligne entre niveaux Z
    fic.close()

    print "fin lecture; ecriture "

    nbZMax = nbz
    if self.nbZ > nbZMax:
      nbZMax = self.nbZ

    fic = open(cheminFichier + "res.txt", "w")
    R = Voxel.resolution
    fic.write("%f %f %f\n%f %f %f\n%d\n" % (self.nbX * R, self.nbY * R, nbZMax * R, R, R, R, self.nbX * self.nbY))
    factor = R * R
    for k in range(nbZMax + 1):
#      print k
      if k >= nbz:
        indiceDansMaket = False
      else:
        indiceDansMaket = True
      for i in range(self.nbX):
        for j in range(self.nbY):
          indice = (i, j, k)
          rho = self.getDensite(indice)
          rhoTotal = 0
          if rho <= 0 or np.isnan(rho):
            if indiceDansMaket:
              rhoTotal = densiteParCellule[indice]
          else:
            if indiceDansMaket:
              rhoTotal = densiteParCellule[indice] + rho
            else:  rhoTotal = rho

          typeCell = 0
          if rhoTotal == 0:
            if indiceDansMaket:
              typeCell = typeCellule[indice]
          else:
            if indiceDansMaket and nbTriangles[indice] > 0:
              typeCell = 16
            else :
              typeCell = 5

          fic.write("%d " % (typeCell))
          if typeCell != 0:
            if indiceDansMaket:
              fic.write("%d " % (nbTriangles[indice]))
              for numTri in range(nbTriangles[indice]):
                fic.write("%d " % (numTrianglesParCellule[indice][numTri]))
            else :
              fic.write("0 ")

            if rhoTotal > 0:
              fic.write("1 %f 0 0 " % (rhoTotal))
            else:
              fic.write("0 0 ")
        fic.write("\n")
      fic.write("\n")
    fic.close()
    print "Fin ajout densite", cheminFichier, "ecrit"

  def ecrireFichierXYZI(self, cheminFichier):
    fic = open(cheminFichier, "w")
    for k in range(self.nbZ):
      for i in range(self.nbX):
        for j in range(self.nbY):
          indice = (i, j, k)
          rho = self.getDensite(indice)
          if rho > 0 and ~np.isnan(rho):
            fic.write("%d %d %d %f\n" % (i, j, k, rho))
#          fic.write("%f %f %f %f\n" %( self.listeVoxels[indice].position[0], self.listeVoxels[indice].position[1], self.listeVoxels[indice].position[2], self.listeVoxels[indice].getDensite()))
    fic.close()
    print "fichier", cheminFichier, "ecrit"



#  def getResolution(self):
#    return 0.5
#    return self.listeVoxels[(0, 0, 1)].position[2] - self.listeVoxels[(0, 0, 0)].position[2]

  def getListeAttributs(self):
    transmitance, densite, nbDedans, nbTotal, lg, S = [], [], [], [], [], []

    for indice in self.listeVoxels.keys():
      i_nbTotal = self.listeVoxels[indice].nbDedans + self.listeVoxels[indice].nbApres

      densite.append(self.getDensite(indice))
      transmitance.append(self.listeVoxels[indice].getTransmittance())
      nbDedans.append(self.listeVoxels[indice].nbDedans)
      nbTotal.append(i_nbTotal)
      lg.append(self.listeVoxels[indice].longueur)
      S.append(self.listeVoxels[indice].surface)

    return np.array(nbDedans), np.array(nbTotal), np.array(transmitance), np.array(densite), np.array(lg), np.array(S)

  def afficheStats(self):
    nbDedans, nbTotal, transmitance, densite, lg, S = self.getListeAttributs()
    print "Dedans :\t" + Stats(nbDedans).getStatsString()
    print "nbTotal :\t" + Stats(nbTotal).getStatsString()
    print "transmitance :\t" + Stats(transmitance).getStatsString()
    print "densite :\t" + Stats(densite).getStatsString()
    print "longueur :\t" + Stats(lg).getStatsString() + " longueurMoyenne : " + str(np.sum(lg[~np.isnan(lg)]) / np.sum(nbTotal[~np.isnan(nbTotal)]))
    print "surface :\t" + Stats(S).getStatsString()

  def ecritStatsTex(self, nomFichierOutput):
    nbDedans, nbTotal, transmitance, densite, lg, S = self.getListeAttributs()
    statis = Stats(nbDedans)
    fic = open(nomFichierOutput, "w")
    fic.write(statis.getStatsStringTexEntete())
    fic.write("nbDedans : & " + Stats(nbDedans).getStatsStringTex())
    fic.write("nbEntrant : & " + Stats(nbTotal).getStatsStringTex())
    fic.write("transmitance : & " + Stats(transmitance).getStatsStringTex())
    fic.write("densite : & " + Stats(densite).getStatsStringTex())
    fic.write("longueurTotale : & " + Stats(lg).getStatsStringTex())  # + " longueurMoyenne : " + str(np.sum(lg[~np.isnan(lg)]) / np.sum(nbTotal[~np.isnan(nbTotal)]))
    fic.write("surfaceVue : & " + Stats(S).getStatsStringTex())
    fic.close()

  def fusionnerMaillages(self, maillage2 , ponderation = False):
    if maillage2.getNbVoxels() != self.getNbVoxels():
      print "les fichiers n'ont pas le meme nombre de voxels..."
      return

    for k in range(self.nbZ):
#      print k
      for i in range(self.nbX):
        for j in range(self.nbY):
          if ponderation:
            self.fusionnerVoxelPondere(maillage2[(i, j, k)])
          else:
            self.fusionnerVoxel(maillage2[(i, j, k)])

  def fusionnerMaillagesListe(self, listeFichierMaillage):
    maillages = []
    for fic in listeFichierMaillage:
      maillages.append(getMaillage(fic))

    for k in range(self.nbZ):
      for i in range(self.nbX):
        for j in range(self.nbY):
          indice = (i, j, k)
          # surface = self.listeVoxels[indice].getSurfaceFaisceauEntrant()
          # longueurIntercept = self.listeVoxels[indice].longueurIntercept * surface
          # nbApres = self.listeVoxels[indice].nbApres * surface
          # nbDedans = self.listeVoxels[indice].nbDedans * surface
          # surfaceTotale = surface
          for maillage in maillages:
            # surface2 = maillage.listeVoxels[indice].getSurfaceFaisceauEntrant()
            # nbApres += maillage.listeVoxels[indice].nbApres * surface2
            # nbDedans += maillage.listeVoxels[indice].nbDedans * surface2
            self.listeVoxels[indice].nbApres += maillage.listeVoxels[indice].nbApres  # / surfaceTotale
            self.listeVoxels[indice].nbDedans += maillage.listeVoxels[indice].nbDedans  # / surfaceTotale
            # surfaceTotale += surface
            self.listeVoxels[indice].longueur += maillage.listeVoxels[indice].longueur
            self.listeVoxels[indice].longueurIntercept += maillage.listeVoxels[indice].longueurIntercept
         # self.listeVoxels[indice].nbApres = nbApres #/ surfaceTotale
          # self.listeVoxels[indice].nbDedans = nbDedans #/ surfaceTotale
          self.listeVoxels[indice].surfaceVue = 1
          self.listeVoxels[indice].distance = 1

  def ajouterInformationFichierDensite(self, nomFichier):
    fic = open(nomFichier, 'r')
    lignes = fic.readlines()
    fic.close()
    i = 0
    for ligne in lignes:
      if i > 0:
        self.fusionnerVoxel(Voxel.lireLigneVoxel(ligne))
      i += 1

  def afficheDensite(self, cheminFichier = "", _show = False):
    X, Y, Z, I, = [], [], [], []
    for indice in self.listeVoxels.keys():
      rho = self.getDensite(indice)
#      print indice, rho
      if ~np.isnan(rho) and rho > 0:
        X.append(indice[0] * Voxel.resolution)
        Y.append(indice[1] * Voxel.resolution)
        Z.append(indice[2] * Voxel.resolution)
        I.append(rho)
        # print "rho", indice, rho
      # else:
        # if indice[0] == 8:
         # print "pasRho", indice, self.getNbTotal(indice), self.getNbDedans(indice) ,

    afficheXYZI(X, Y, Z, I, Voxel.resolution, cheminFichier, mode = 'cube', titre = 'Uf', _show = _show)

  def afficheNtotal(self, cheminFichier = "", _show = False):
    X, Y, Z, I, = [], [], [], []
    for indice in self.listeVoxels.keys():
      rho = self.getNbTotal(indice)
#      print indice, rho
      if ~np.isnan(rho) and rho > 0:
        X.append(indice[0] * Voxel.resolution)
        Y.append(indice[1] * Voxel.resolution)
        Z.append(indice[2] * Voxel.resolution)
        I.append(rho)
        # print "Ntotql", indice, rho
    afficheXYZI(X, Y, Z, I, Voxel.resolution, cheminFichier, mode = 'cube', titre = 'Ntotal', _show = _show)

  def afficheNdedans(self, cheminFichier = "", _show = False):
    X, Y, Z, I, = [], [], [], []
    for indice in self.listeVoxels.keys():
      rho = self.getNbDedans(indice)
#      print indice, rho
      if ~np.isnan(rho) and rho > 0:
        X.append(indice[0] * Voxel.resolution)
        Y.append(indice[1] * Voxel.resolution)
        Z.append(indice[2] * Voxel.resolution)
        I.append(rho)
        # print "Ndedans", indice, rho
    afficheXYZI(X, Y, Z, I, Voxel.resolution, cheminFichier, mode = 'cube', titre = 'NDedans', _show = _show)

  def afficheNapres(self, cheminFichier = "", _show = False):
    X, Y, Z, I, = [], [], [], []
    for indice in self.listeVoxels.keys():
      rho = self.getNbApres(indice)
#      print indice, rho
      if ~np.isnan(rho) and rho > 0:
        X.append(indice[0] * Voxel.resolution)
        Y.append(indice[1] * Voxel.resolution)
        Z.append(indice[2] * Voxel.resolution)
        I.append(rho)
    afficheXYZI(X, Y, Z, I, Voxel.resolution, cheminFichier, mode = 'cube', titre = 'NApres', _show = _show)

  def afficheDensiteEtCellulesPleines(self, cheminFichier = "", _show = True):
    X, Y, Z, I, = [], [], [], []
    X2, Y2, Z2, I2, = [], [], [], []
    X3, Y3, Z3, I3, = [], [], [], []
    for indice in self.listeVoxels.keys():
      rho = self.getDensite(indice)
#      print indice, rho
      if ~np.isnan(rho) and rho > 0:
        if rho <= 3 and rho >= 0.3:
          X.append(indice[0] * Voxel.resolution)
          Y.append(indice[1] * Voxel.resolution)
          Z.append(indice[2] * Voxel.resolution)
          I.append(rho)
        elif rho < 3:
          X2.append(indice[0] * Voxel.resolution)
          Y2.append(indice[1] * Voxel.resolution)
          Z2.append(indice[2] * Voxel.resolution)
          I2.append(rho)
        else:
          X3.append(indice[0] * Voxel.resolution)
          Y3.append(indice[1] * Voxel.resolution)
          Z3.append(indice[2] * Voxel.resolution)
          I3.append(rho)
    afficheXYZI(X, Y, Z, I, Voxel.resolution, cheminFichier, mode = 'cube', titre = 'Uf', _show = _show)
    afficheXYZI(X2, Y2, Z2, I2, Voxel.resolution, cheminFichier, mode = 'cube', titre = 'Uf', _show = _show)
    afficheXYZI(X3, Y3, Z3, I3, Voxel.resolution, cheminFichier, mode = 'cube', titre = 'Uf', _show = _show)

  def afficheSurface(self):
    X, Y, Z, I, = [], [], [], []
    for indice in self.listeVoxels.keys():
#      if self.listeVoxels[indice].getAngleSolide() > 0:
        X.append(indice[0])
        Y.append(indice[1])
        Z.append(indice[2])

        I.append(self.listeVoxels[indice].getAngleSolide())
    afficheXYZI(X, Y, Z, I, 1, titre = 'Angle solide')

  def lireFichierMaille(self, cheminFichier):
    fic = open(cheminFichier, 'r')
    lignes = fic.readlines()
    fic.close()
    i = 0
    for ligne in lignes:
      lig = map(float, ligne.rstrip('\n\r').split())
      indice = (int(lig[0]), int(lig[1]), int(lig[2]))
      if not indice in self.listeVoxels:
        self.listeVoxels[indice] = Voxel(indice, 0, 0, 0, 0, 0, 0)
      self.listeVoxels[indice].position = (lig[3], lig[4], lig[5])
      self.listeVoxels[indice].angleSolide = lig[6]
      self.listeVoxels[indice].distanceAuScanner = lig[7]

  def getListeIndices(self):
    return self.listeVoxels.keys()

  def getListeDensite(self, listeIndices):
    densite = []
    for indice in listeIndices:
      if indice in self.listeVoxels:
        densite.append(self.getDensite(indice))
      else:
        densite.append(np.nan)
    return densite

  def getListeDensiteNonNan(self, listeIndices):
    densite = []
    for indice in listeIndices:
      if indice in self.listeVoxels:
        rho = self.getDensite(indice)
        if not np.isnan(rho):
          densite.append(rho)
    return densite

  def getListeDensiteNonNanNonNul(self, listeIndices):
    densite = []
    for indice in listeIndices:
      if indice in self.listeVoxels:
        rho = self.getDensite(indice)
        if not np.isnan(rho) and rho > 0:
          densite.append(rho)
    return densite

  def getListeDensite2Maillages(self, maillage):
    listeIndices = self.getListeIndices()
    densite_1 = self.getListeDensite(listeIndices)
    densite_2 = maillage.getListeDensite(listeIndices)

    return densite_1, densite_2

  def getRegression(self, maillage):
    densite_1, densite_2 = self.getListeDensite2Maillages(maillage)
    return getCoeffRegression(densite_1, densite_2)

  def afficheRegression(self, maillage):
    gradient, intercept, r_value, p_value, std_err = self.getRegression(maillage)

    fig = plt.figure()
    ax1 = fig.add_subplot(111)
    ecritRegressionSurFigure(ax1, densite_1, densite_2)
    plt.show()


class Voxel:
  seuilCalculDensite = 100
  resolution = 1
  priseEnCompteG = False

  @staticmethod
  def lireLigneVoxel(ligne, type):
    v_ligne = ligne.strip().split()
    n = 0
    if len(v_ligne) == 11:
      n = 1
    elif len(v_ligne) != 10:
      print "erreur ligne ", v_ligne
      exit()
      return Voxel((-1, -1, -1), 0, 0, 0, 0, 0, 0, 0)
    if type == "classic":
      i, j, k, lg, lgIntercept, intercept, nbApres, surfaceVue, Distance, densite = v_ligne[0], v_ligne[1], v_ligne[2], v_ligne[3], v_ligne[4], v_ligne[5], v_ligne[6], v_ligne[7 + n], v_ligne[8 + n], v_ligne[9 + n]
    else:
      i, j, k, lg, lgIntercept, intercept, nbApres, surfaceVue, Distance, densite = v_ligne[0], v_ligne[1], v_ligne[2], 0, 0, 0, 0, 0, 0, v_ligne[9]


    return Voxel((int(i), int(j), int(k)), float(lg), float(lgIntercept), float(intercept), float(nbApres), float(surfaceVue), float(Distance), float(densite))

  def __init__(self, indice, longueur, longueurIntercept, nbDedans, nbApres, surfaceVue, distance, densite):
    self.indice = indice
    self.longueur = longueur
    self.longueurIntercept = longueurIntercept
    self.nbApres = nbApres
    self.nbDedans = nbDedans
    self.surfaceVue = surfaceVue
    self.densite = densite
    self.distanceAuScanner = distance
#    print indice, longueur, longueurIntercept, nbApres, nbDedans, surface, densite

  def __str__(self):
    return str(self.indice[0]) + " " + str(self.indice[1]) + " " + str(self.indice[2]) + " rho = " + str(self.densite) + " lg = " + str(self.longueur) + " lgI = " + str(self.longueurIntercept) + " i = " + str(self.nbDedans) + " apres = " + str(self.nbApres)

#  def getDistance(self):
#    return np.sqrt(self.getDistanceCarree())

#  def getDistanceCarree(self):
#    return self.position[0]* self.position[0] + self.position[1]* self.position[1] + self.position[2]* self.position[2]
  def getAngleSolide(self):
    if self.distanceAuScanner <= 0:
      return 4 * np.pi
    return self.surfaceVue / (self.distanceAuScanner * self.distanceAuScanner)

  def getSurfaceFaisceauEntrant(self):
    # la surface est  egale au (diametre a la sortie de l'instrument + tan(divergence)*distance) **2 * pi/4
    d0 = 0.007  # diametre d'ouverture
#    print "distance : ", self.distanceAuScanner, "S", ((d0 + np.tan(0.00035) * self.distanceAuScanner) ** 2) * np.pi / 4
    return ((d0 + np.tan(0.00035) * self.distanceAuScanner) ** 2) * np.pi / 4

  def getNbTotal(self):
    return self.nbDedans + self.nbApres

  def getLongueurMoyenne(self):
    if self.longueur > 0:
#        if  self.nbDedans > 0:
#          return self.longueur / self.nbApres + self.longueurIntercept / self.nbDedans
#        else:
#          print Voxel.resolution, self.longueur / self.getNbTotal()
#      print self.longueur / self.getNbTotal()
      if self.nbApres == 0:
        print "longueur nulle", self.indice
        return np.nan
        #exit()
      return self.longueur / self.nbApres
    else:
      return np.nan
#    if modeCalculDensite == listeMethodes[0]:
#      if self.longueur > 0:
# #        if  self.nbDedans > 0:
# #          return self.longueur / self.nbApres + self.longueurIntercept / self.nbDedans
# #        else:
# #          print Voxel.resolution, self.longueur / self.getNbTotal()
#          return self.longueur / self.getNbTotal()
#      else: return Voxel.resolution
#    elif modeCalculDensite == listeMethodes[1]:
#    elif modeCalculDensite == listeMethodes[3]:
#      return self.angleSolide
#    else:
#      return Voxel.resolution

  def getLongueurParcourtMoyen(self):
    if self.nbDedans > 0:
      # print self.longueurIntercept / self.nbDedans, Voxel.resolution, self.longueur / self.nbApres, (self.longueurIntercept +  self.longueur) / (self.nbApres+ self.nbDedans)
      return (self.longueurIntercept + self.longueur) / (self.nbApres + self.nbDedans)
    return np.nan

  def getLongueurAvantInterceptMoyen(self):
    if self.nbDedans > 0:
      # print self.longueurIntercept / self.nbDedans, Voxel.resolution, self.longueur / self.nbApres, (self.longueurIntercept +  self.longueur) / (self.nbApres+ self.nbDedans)
      return self.longueurIntercept / self.nbDedans
    return np.nan

  def getTransmittance(self, modeCalculDensite = listeMethodes[1]):
    total = self.getNbTotal()
    if total < Voxel.seuilCalculDensite: return np.nan
    if self.nbDedans == 0: return 1
    if modeCalculDensite == listeMethodes[0]:
      transmitance = 1 - self.nbDedans / total
    elif modeCalculDensite == listeMethodes[1]:
      transmitance = 1 - self.nbDedans / total
    elif modeCalculDensite == listeMethodes[2]:
      transmitance = 1 - self.nbDedans / total
    elif modeCalculDensite == listeMethodes[3]:
      transmitance = 1 - (self.nbDedans / total)

    else:
      print "methode", modeCalculDensite, " non implementee pour le calcul de la transmittance"
#      print 'mode 2', transmitance, 1 - self.nbDedans / total

    if transmitance == 0: return np.nan
    else: return transmitance

  def getG(self):
    if Voxel.priseEnCompteG and hasattr(self, "G"):
      if self.G == 0:
        self.G = 0.0001
      return self.G
    return 0.5

  def getDensite(self, modeCalculDensite = listeMethodes[1]):
    if self.getNbTotal() < Voxel.seuilCalculDensite or np.isnan(self.getNbTotal()):
      # print self.getNbTotal()
      return np.nan
    G = self.getG()
    # if self.indice == (0, 0, 8):
    #  print modeCalculDensite, self

    if modeCalculDensite == listeMethodes[1]:
      transmittance = self.getTransmittance(modeCalculDensite)
      rho = -np.log(transmittance * G)

    elif modeCalculDensite == listeMethodes[0]:
      transmittance = self.getTransmittance(modeCalculDensite)
      # rho =  - np.log(transmittance) / (G * self.getLongueurMoyenne() / self.getLongueurParcourtMoyen())
      rho = -np.log(transmittance) / (G * self.getLongueurMoyenne())
      # return - np.log(transmittance) / (G * self.getLongueurMoyenne())

    elif modeCalculDensite == listeMethodes[2]:
      transmittance = self.getTransmittance(modeCalculDensite)
      # rho = -np.log(transmittance) / (G * Voxel.resolution / self.getLongueurMoyenne())
      rho = -np.log(transmittance) / (G * self.getLongueurParcourtMoyen())

    elif modeCalculDensite == listeMethodes[3]:
      transmittance = self.getTransmittance(modeCalculDensite)
      rho = -np.log(transmittance) / (G * self.getLongueurParcourtMoyen() / self.getLongueurMoyenne())

#    elif modeCalculDensite == listeMethodes[2]:
#      if self.longueur == 0:
#        print "erreurImpossible entrant: ", self.getNbTotal(), "seuil", Voxel.seuilCalculDensite, "dedans", self.nbDedans, "lg : ", self.longueur
#        return 0
#      return self.nbDedans / (G * self.longueur)

    elif modeCalculDensite == listeMethodes[4]:
      rho = self.nbDedans / (self.getG() * self.longueur)
      print rho

    # print "getDensiteee", rho, modeCalculDensite, transmittance, G
    if rho > 10: return np.nan
    if rho < 0:
      print "rho negatif", rho, self.indice, modeCalculDensite, transmittance
      return np.nan
    return rho

  def getDensiteStockee(self):
    if self.densite == -1 or np.isnan(self.densite): return 0
    return self.densite

  def RAZ(self):
    self.longueur = 0
    self.longueurIntercept = 0
    self.nbApres = 0
    self.nbDedans = 0
    self.densite = np.nan

class Stats:
  def __init__(self, data):
    self.dataAvecNan = data
    self.nbTotal = len(data)
    self.nbNan = 0

    self.data = []

    for i in range(self.nbTotal):
      if not np.isnan(self.dataAvecNan[i]): self.data.append(self.dataAvecNan[i])
      else: self.nbNan += 1

  def getStatsString(self):
    return "N = " + str(self.nbTotal) + " min/max : " + str(np.min(self.data)) + " " + str(np.max(self.data)) + "\tMoy = " + str(np.mean(self.data)) + " std = " + str(np.std(self.data)) + "\tNb_NAN = " + str(self.nbNan)

  def getStatsStringTexEntete(self):
    return "N\\_total : " + str(self.nbTotal) + " ; N\\_nan : " + str(self.nbNan) + "\n \\begin{tabular}[c]{lcccc}\n  & min & max & moyenne & stdev\\\\ \n"

  def getStatsStringTex(self):
    return str(round(np.min(self.data), 3)) + " & " + str(round(np.max(self.data), 3)) + " & " + str(round(np.mean(self.data), 3)) + " & " + str(round(np.std(self.data), 3)) + "\\\\ \n"



def getVecteurNonNAN(X_1):
  return [x for x in X_1 if not np.isnan(x)]

def getVecteursNonNAN(X_1, X_theorique_1):
  X = []
  X_theorique = []
#  nBNan = 0
  for i in range(len(X_1)):
    if not np.isnan(X_1[i]) and not np.isnan(X_theorique_1[i]):
      X.append(X_1[i])
      X_theorique.append(X_theorique_1[i])
#    else:
#      nBNan += 1
#  print nBNan
  return X, X_theorique

def getRMSE(X_1, X_theorique_1):
  X, X_theorique = getVecteursNonNAN(X_1, X_theorique_1)
  MSE, n = 0.0, 0
  for i in range(len(X)):
    if X_theorique[i] > 0:
      MSE += ((X[i] - X_theorique[i])) ** 2
      n = n + 1
  if n == 0:
    return 0
  RMSE = np.sqrt(MSE / n)
  return RMSE

def getRMSD(X_1, X_theorique_1):
  return getRMSE(X_1, X_theorique_1) / np.mean(X_theorique_1)

def getCoeffRegression(X_1, X_theorique_1):
  X, X_theorique = getVecteursNonNAN(X_1, X_theorique_1)

  if len(X) == 0:
    print "erreur : pas de valeurs trouvees pour regression"
    return 0, 0, 0, 0, 0
  return stats.linregress(X, X_theorique)

def getPenteRegression(X_1, X_theorique_1):
  X, X_theorique = getVecteursNonNAN(X_1, X_theorique_1)

  if len(X) == 0:
    print "erreur : pas de valeurs trouvees pour regression"
    return 0
  return stats.linregress(X_theorique, X)[0]

def getR2Regression(X_1, X_theorique_1):
  X, X_theorique = getVecteursNonNAN(X_1, X_theorique_1)

  if len(X) == 0:
    print "erreur : pas de valeurs trouvees pour regression"
    return 0
  return stats.linregress(X_theorique, X)[2] ** 2

def ecritRegressionSurFigureOld(ax, X_1, X_theorique_1, afficheRegression = True):
  X, X_theorique = getVecteursNonNAN(X_1, X_theorique_1)

  if len(X) == 0:
    print "erreur : pas de valeurs trouvees pour regression", X_1, X_theorique_1
    return
  gradient, intercept, r_value, p_value, std_err = stats.linregress(X_theorique, X)

  # print gradient, intercept, r_value*r_value, p_value, std_err

  RMSE = getRMSE(X, X_theorique)
  RMSD = getRMSD(X, X_theorique)

  grad = []
  # print "x",X
  # print "x2",X_theorique
  # print gradient, intercept, r_value, p_value, std_err
  for i in X_theorique:
    grad.append(i * gradient + intercept)

  tex = "$RMSE = " + str(np.round(RMSE, 3)) + '\\ RMSD = ' + str(np.round(RMSD, 3)) + "R^2=" + str(np.round(r_value * r_value, 3)) + '\\ p = ' + str(np.round(gradient, 3)) + '\\ r = ' + str(np.round(intercept, 3)) + ' $'
  # tex2 = "$RMSE = "+str(np.round(RMSE * r_value, 3)) +  '\\ RMSD = ' + str(np.round(RMSD, 3)) + ' $'

  ax.plot(X_theorique, X, '+')
  if afficheRegression:
    xlimites = ax.get_xlim()
    ylimites = ax.get_ylim()
    ax.plot(X_theorique, grad, 'g-')
    ax.plot(X_theorique, X_theorique, 'k-')
    ax.text(xlimites[0] + (xlimites[1] - xlimites[0]) / 10, ylimites[1] - (ylimites[1] - ylimites[0]) / 4, tex, fontsize = 15)
  # ax.text(xlimites[0] + (xlimites[1] - xlimites[0]) / 3, ylimites[1] - 2*(ylimites[1] - ylimites[0]) / 4, tex2, fontsize = 15)

def ecritRegressionSurFigure(ax, X_1, X_theorique_1, afficheRegression = True, affichePoints = True):
  X, X_theorique = getVecteursNonNAN(X_1, X_theorique_1)

  if len(X) == 0:
    print "erreur : pas de valeurs trouvees pour regression", X_1, X_theorique_1
    return
  gradient, intercept, r_value, p_value, std_err = stats.linregress(X_theorique, X)

  # print gradient, intercept, r_value*r_value, p_value, std_err

  RMSE = getRMSE(X, X_theorique)
  RMSD = getRMSD(X, X_theorique)

  grad = []
  # print "x",X
  # print "x2",X_theorique
  # print gradient, intercept, r_value, p_value, std_err
  for i in X_theorique:
    grad.append(i * gradient + intercept)

  if affichePoints:
    ax.plot(X_theorique, X, '+')
  if afficheRegression:
    xlimites = ax.get_xlim()
    ylimites = ax.get_ylim()
    ax.plot(X_theorique, grad, 'g-')
    ax.plot(X_theorique, X_theorique, 'k-')
    # ax.text(xlimites[0] + (xlimites[1] - xlimites[0]) / 10, ylimites[1] - (ylimites[1] - ylimites[0]) / 4, tex, fontsize = 15)
  # ax.text(xlimites[0] + (xlimites[1] - xlimites[0]) / 3, ylimites[1] - 2*(ylimites[1] - ylimites[0]) / 4, tex2, fontsize = 15)

  from matplotlib.lines import Line2D
  l1 = Line2D([], [], linewidth = 0, color = "k", marker = 'None')
  l2 = Line2D([], [], linewidth = 0, color = "k", marker = 'None')
  l3 = Line2D([], [], linewidth = 0, color = "k", marker = 'None')
  l4 = Line2D([], [], linewidth = 0, color = "k", marker = 'None')

  ff = plt.legend([l1, l2, l3, l4], ["$RMSE = " + str(np.round(RMSE, 3)) + "$", "$RMSD = " + str(np.round(RMSD, 3)) + " $", "$R^2=" + str(np.round(r_value * r_value, 3)) + "$", "$p = " + str(np.round(gradient, 3)) + "$"], prop = {"size":14}, borderpad = 0, frameon = True, markerscale = 0, loc = "best")
  frame = ff.get_frame()
  frame.set_alpha(0.75)
  frame.set_linewidth(0)
  # mpl.rc['legend.numpoints'] = 0

  # frame.set_facecolor("white")

def ecritRegressionSurFigure3(ax, X_1, X_theorique_1, afficheRegression = True, affichePoints = True):
  X, X_theorique = getVecteursNonNAN(X_1, X_theorique_1)

  if len(X) == 0:
    print "erreur : pas de valeurs trouvees pour regression", X_1, X_theorique_1
    return
  gradient, intercept, r_value, p_value, std_err = stats.linregress(X_theorique, X)

  # print gradient, intercept, r_value*r_value, p_value, std_err

  RMSE = getRMSE(X, X_theorique)
  RMSD = getRMSD(X, X_theorique)

  grad = []
  # print "x",X
  # print "x2",X_theorique
  # print gradient, intercept, r_value, p_value, std_err
  for i in X_theorique:
    grad.append(i * gradient + intercept)

  if affichePoints:
    ax.plot(X_theorique, X, '+')
  if afficheRegression:
    xlimites = ax.get_xlim()
    ylimites = ax.get_ylim()
    ax.plot(X_theorique, grad, 'g-')
    ax.plot(X_theorique, X_theorique, 'k-')
    # ax.text(xlimites[0] + (xlimites[1] - xlimites[0]) / 10, ylimites[1] - (ylimites[1] - ylimites[0]) / 4, tex, fontsize = 15)
  # ax.text(xlimites[0] + (xlimites[1] - xlimites[0]) / 3, ylimites[1] - 2*(ylimites[1] - ylimites[0]) / 4, tex2, fontsize = 15)

  from matplotlib.lines import Line2D
  l1 = Line2D([], [], linewidth = 0, color = "k", marker = 'None')
  l2 = Line2D([], [], linewidth = 0, color = "k", marker = 'None')
  l3 = Line2D([], [], linewidth = 0, color = "k", marker = 'None')

  ff = ax.legend([l1, l2, l3], ["$RMSE = " + str(np.round(RMSE, 3)) + "$", "$R^2=" + str(np.round(r_value * r_value, 3)) + "$", "$p = " + str(np.round(gradient, 3)) + "$"], prop = {"size":14}, borderpad = 0, frameon = True, markerscale = 0, loc = "best")
  frame = ff.get_frame()
  frame.set_alpha(0.75)
  frame.set_linewidth(0)

def ecritRegressionSurFigure2(ax, ax2, X_1, X_theorique_1, afficheRegression = True, affichePoints = True):
  X, X_theorique = getVecteursNonNAN(X_1, X_theorique_1)

  if len(X) == 0:
    print "erreur : pas de valeurs trouvees pour regression", X_1, X_theorique_1
    return
  gradient, intercept, r_value, p_value, std_err = stats.linregress(X_theorique, X)


  grad = []
  # print "x",X
  # print "x2",X_theorique
  # print gradient, intercept, r_value, p_value, std_err
  for i in X_theorique:
    grad.append(i * gradient + intercept)

  if affichePoints:
    ax.plot(X_theorique, X, '+')
  if afficheRegression:
    ax.plot(X_theorique, grad, 'g-')
    # ax.text(xlimites[0] + (xlimites[1] - xlimites[0]) / 10, ylimites[1] - (ylimites[1] - ylimites[0]) / 4, tex, fontsize = 15)
  # ax.text(xlimites[0] + (xlimites[1] - xlimites[0]) / 3, ylimites[1] - 2*(ylimites[1] - ylimites[0]) / 4, tex2, fontsize = 15)

  from matplotlib.lines import Line2D
  l1 = Line2D([], [], linewidth = 0, color = "k", marker = 'None')
  l2 = Line2D([], [], linewidth = 0, color = "k", marker = 'None')


  ff = ax2.legend([l1, l2], [ "$R^2=" + str(np.round(r_value * r_value, 3)) + "$", "$p = " + str(np.round(gradient, 3)) + "$"], prop = {"size":14}, borderpad = 0, frameon = True, markerscale = 0, scatterpoints = 1, loc = "best")
  frame = ff.get_frame()
  frame.set_alpha(0.75)
  frame.set_linewidth(0)

def ecritStandardDevSurFigure(ax, X_1, ecritN = False):
  X = getVecteurNonNAN(X_1)
  stdev = np.std(X)
  mean = np.mean(X)

  l1 = Line2D([], [], linewidth = 0, marker = 'None', color = "k")
  l2 = Line2D([], [], linewidth = 0, marker = 'None', color = "k")

  if ecritN:

    ff = plt.legend([l1, l2], ["$N = " + str (len(X)) + "$", "$\sigma=" + str(np.round(stdev, 3)) + "$"], prop = {"size":14}, borderpad = 0, frameon = True, markerscale = 0, loc = "lower right")

  else:
    ff = plt.legend([l1, l2], ["$mean=" + str(np.round(mean, 3)) + "$", "$\sigma=" + str(np.round(stdev, 3)) + "$"], prop = {"size":14}, borderpad = 0, frameon = True, markerscale = 0, loc = "lower right")
  frame = ff.get_frame()
  frame.set_alpha(0.75)
  frame.set_linewidth(0)

def getListeFichierDensiteParScan(dossierResultat):
  listeScans = []
  for i in range(20):
    if i < 10:
      chemin = "ScanPos00" + str(i)
    elif i < 100:
      chemin = "ScanPos0" + str(i)
    else:
      chemin = "ScanPos" + str(i)
    listeScans.append(chemin)

  # cas 1 seul fichier rxp
  listeScans.append("scanTest")
  # eloi temporaire
  listeScans.append("ScanPos2b")
  listeScans.append("ScanPos3b")
  listeScans.append("ScanPos4b")

  listeFichierParScan = []
  for scan in listeScans:
    chemin = dossierResultat + "densite/densite" + str(scan) + "*"
    if len(glob.glob(chemin)) > 0:
      listeFichierParScan.append(glob.glob(chemin)[0])

  return listeFichierParScan




def fusionnerFichierDensite(dossierResultat):
  listeFichierParScan = getListeFichierDensiteParScan(dossierResultat)

  print listeFichierParScan[0]
  maillageTotal = getMaillage(listeFichierParScan[0])
  for i in range(len(listeFichierParScan) - 1):
    print listeFichierParScan[i + 1]
    maillageTotal.ajouterInformationFichierDensite(listeFichierParScan[i + 1])
  return maillageTotal

def getMaillageMaket(nomFichier, seuil = 100):
  print "chargement...",
  fic = open(nomFichier, 'r')
  lignes = fic.readlines()
  fic.close()
  maillage = Maille()
  Voxel.seuilCalculDensite = seuil
  i = 0
  maillage.nomFichier = nomFichier
  lig = map(float, lignes[0].rstrip('\n\r').split())
  sceneDim = Point3D(lig[0], lig[1], lig[2])
  lig = map(float, lignes[1].rstrip('\n\r').split())
  cellDim = Point3D(lig[0], lig[1], lig[2])
  if cellDim.x != cellDim.y or cellDim.x != cellDim.z or cellDim.y != cellDim.z:
    print "cellules non cubiques... arret du process"
    return
  nbCell = Point3D(int(sceneDim.x / cellDim.x), int(sceneDim.y / cellDim.y), int(sceneDim.z / cellDim.z))
  maillage.initialiser(nbCell.x, nbCell.y, nbCell.z)
  Voxel.resolution = cellDim.x

  factor = cellDim.x * cellDim.y
  print "cellDim : ", cellDim, "NbCell : ", nbCell
  id_lig = 3
#  for k in range(1):
  for k in range(nbCell.z):
    for i in range(nbCell.x):
      lig = map(float, lignes[id_lig].rstrip('\n\r').split())
      indx = 0
      for j in range(nbCell.y):
        typeCell = lig[indx]
        indx += 1
        if typeCell > 0:
          nbTri = int(lig[indx])
          indx += nbTri + 1
          nbTurb = int(lig[indx])
          indx += 1
#          print typeCell, i, j, k, "nbtri:",nbTri,"nbturb:", nbTurb, "ligCourant", lig[indx], indx
          lai = 0
          for l in range(nbTurb):
            lai += lig[indx]
            indx += 1
          maillage.ajouterVoxel(Voxel((i, j, k), 0, 0, 0, 0, 0, 0, lai / cellDim.z))
          if nbTurb == 0:
            indx += 1
          else:
            indx += 2
        else:
          maillage.ajouterVoxel(Voxel((i, j, k), 0, 0, 0, 0, 0, 0, -1))

#          print i, j, k, indx, nbTri, nbTurb, densite
#        else:
#          print typeCell, i, j, k, indx
      id_lig += 1
    id_lig += 1

  maillage.type = "dart"
  print "fin chargement ", nomFichier, ";"
  return maillage


def getMaillage(nomFichier, seuil = 100, type = 'classic'):
  print "chargement...", nomFichier, 
  fic = open(nomFichier, 'r')
  lignes = fic.readlines()
  fic.close()
  maillage = Maille()
  maillage.nomFichier = nomFichier
  i = 0
  Voxel.seuilCalculDensite = seuil
  for ligne in lignes:
    if i > 0:
      maillage.ajouterVoxel(Voxel.lireLigneVoxel(ligne, type))
    else:
      v_ligne = ligne.strip().split()
      nbX, nbY, nbZ, R = int(v_ligne[0]), int(v_ligne[1]), int(v_ligne[2]), float(v_ligne[3])
      maillage.initialiser(nbX, nbY, nbZ)
      Voxel.resolution = R
    i += 1
  print "fin chargement ", ";", nbX, nbY, nbZ
  maillage.type = type

  if Voxel.priseEnCompteG:
    maillage.ajouterFonctionG(nomFichier + "_G.txt")

  return maillage



class VoxelisationEXE():
  def __init__(self, cheminProprietes = ""):
    self.cheminExecutable = "/media/DATA/workspace/CPP/voxelisationCpp/src/build/voxelisation.exe"
    self.fichierLogExtraction = ""
    self.properties = FichierProprietes(cheminProprietes)

  def executerFP(self):
    if self.fichierLogExtraction != "":
      commande = self.cheminExecutable + " -i " + self.properties.cheminFichier + " 1>" + self.fichierLogExtraction + " 2>" + self.fichierLogExtraction
    else:
      commande = self.cheminExecutable + " -i " + self.properties.cheminFichier
    print "COMMANDE ", commande
#    subprocess.check_call(commande, shell = True)
    subprocess.Popen(commande, shell = True)
#    time.sleep(3)

  def executerProprietes(self, newFicprop, fichierXYZ, dossierResultat, fichierSortieDensite, resolution, typeMaillage, typeMultiEcho = 0, min = [], max = []):
#    if not os.path.isfile(newFicprop):
      self.properties["fichierSortieDensite"] = fichierSortieDensite
      self.properties["typeMaillage"] = typeMaillage
      self.properties["fichierXYZ"] = fichierXYZ
      self.properties["resolution"] = resolution
      self.properties.cheminFichier = newFicprop
      self.properties["dossierOutput"] = dossierResultat
      self.properties["appliquerMatriceSOPSupplementaire"] = 0
      self.properties["methodePonderationEchos"] = 0
      self.properties["thetaFiltre"] = 0
      self.properties["typeMultiEcho"] = typeMultiEcho
      self.properties["typeExecution"] = 3
      if len(min) != 0:
        self.properties["pointMailleMin.x"] = min[0]
        self.properties["pointMailleMin.y"] = min[1]
        self.properties["pointMailleMin.z"] = min[2]
      if len(max) != 0:
        self.properties["pointMailleMax.x"] = max[0]
        self.properties["pointMailleMax.y"] = max[1]
        self.properties["pointMailleMax.z"] = max[2]
      self.properties.ecrireFichier()
      self.executerFP()
#    else:
#      print "voxelisation deja calculee", newFicprop

