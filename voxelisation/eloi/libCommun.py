#-*- coding: utf-8 -*-
'''
Created on 16 nov. 2013

@author: grau
'''
import numpy as np
import matplotlib.pyplot as plt
import utm as utm
import sys, shutil, os, random, subprocess, re, glob
import scipy.signal
#from mayavi import mlab


cLumiere = 0.299792458   #m/ns
RAD2DEG = 57.2957795130823
DEG2RAD = np.pi / 180
ListColors = ['blue', 'green', 'grey', 'yellow', 'darkmagenta', 'chocolate', 'black', 'darkorange', 'olive', 'black']


class Point3:
  """Classe point contenant (x, y, z)"""
  #constructeur de la classe    
  def __init__(self, vecteur):
    self.x = vecteur[0]
    self.y = vecteur[1]
    self.z = vecteur[2]

  def __getitem__(self, key):
    if key == 0:
      return  self.x
    elif key == 1:
      return  self.y
    elif key == 2:
      return  self.z
    else :
      print "erreur d accesseur de la classe Point3D : pas de cle :", key

  def __str__(self):
    return str(self.x) + ", " + str(self.y) + ", " + str(self.z)

  def __repr__(self):
    return str(self.x) + ", " + str(self.y) + ", " + str(self.z)
  #fcts de la classe Point3
  def getvecteur(self):
    return [self.x, self.y, self.z]

  def miseAJourDuMin(self, pointAcomparer):
    if self.x > pointAcomparer.x:
      self.x = pointAcomparer.x

    if self.y > pointAcomparer.y:
      self.y = pointAcomparer.y

    if self.z > pointAcomparer.z:
      self.z = pointAcomparer.z

  def miseAJourDuMax(self, pointAcomparer):
    if self.x < pointAcomparer.x:
      self.x = pointAcomparer.x

    if self.y < pointAcomparer.y:
      self.y = pointAcomparer.y

    if self.z < pointAcomparer.z:
      self.z = pointAcomparer.z

  def miseAJourDuMinXY(self, x, y):
    if self.x > x:
      self.x = x

    if self.y > y:
      self.y = y

  def miseAJourDuMaxXY(self, x, y):
    if self.x < x:
      self.x = x

    if self.y < y:
      self.y = y
      
class Point3D:
  def __init__(self, _x = 0, _y = 0, _z = 0, calcSphe = False):
    self.x = _x
    self.y = _y
    self.z = _z
    self.calcSphe = calcSphe

    if calcSphe:
      self.recalculeCoordonneesSpheriques()

  def __str__(self):
    if self.calcSphe:
      return "cart : " + str(self.x) + " " + str(self.y) + " " + str(self.z) + " " + ", sphe : " + str(self.r) + " " + str(self.theta * RAD2DEG) + " " + str(self.phi * RAD2DEG)
    else:
      return "(" + str(self.x) + " " + str(self.y) + " " + str(self.z) + ")"

  def __repr__(self):
    if self.calcSphe:
      return "cart : " + str(self.x) + " " + str(self.y) + " " + str(self.z) + " " + ", sphe : " + str(self.r) + " " + str(self.theta * RAD2DEG) + " " + str(self.phi * RAD2DEG)
    else:
      return "(" + str(self.x) + " " + str(self.y) + " " + str(self.z) + ")"

  def __getitem__(self, key):
    if key == 0:
      return  self.x
    elif key == 1:
      return  self.y
    elif key == 2:
      return  self.z
    else :
      print "erreur d accesseur de la classe Point3D : pas de cle :", key

  def __setitem__(self, key, val):
    if key == 0:
      self.x = val
    elif key == 1:
      self.y = val
    elif key == 2:
      self.z = val
    else :
      print "erreur d accesseur de la classe Point3D : pas de cle :", key

  def __add__(self, point) :
    self.x += point.x
    self.y += point.y
    self.z += point.z
    return self

  def __sub__(self, point) :
    self.x -= point.x
    self.y -= point.y
    self.z -= point.z
    return self

  def __mul__(self, point) :
    self.x *= point.x
    self.y *= point.y
    self.z *= point.z
    return self

  def __div__(self, point) :
    self.x /= point.x
    self.y /= point.y
    self.z /= point.z
    return self

  def initSpherique(self, _r, _teta, _phi):
    self.r = _r
    self.theta = _teta
    self.phi = _phi
    self.recalculeCoordonneesCartesiennes()

  def valeurEntiere(self):
    self.x = int(self.x)
    self.y = int(self.y)
    self.z = int(self.z)

  def diviserVal(self, val) :
    self.x /= val
    self.y /= val
    self.z /= val
    return self

  @staticmethod
  def getProjection(lat, lon, alt):
    coord = utm.from_latlon(lat, lon)
    return Point3D(coord[0], coord[1], alt)
  
  @staticmethod
  def getProjectionVecteur(lat_lon_alt):
    coord = utm.from_latlon(lat_lon_alt[0], lat_lon_alt[1])
    return Point3D(coord[0], coord[1], lat_lon_alt[2])

  def calculeProjection(self, lat, lon, alt):
#    flat = 1/298.257223563
#    ls = np.arctan((1 - flat)**2 * np.tan(lat))
#    rad = np.sqrt(6378137 **2 /(1 +(1/((1-flat)**2-1))*np.sin(ls)**2))
#    print "ls", ls, rad
#    self.x = rad * np.cos(ls) * np.cos(lon) + alt * np.cos(lat) * np.cos(lon)
#    self.y = rad * np.cos(ls) * np.sin(lon) + alt * np.cos(lat) * np.sin(lon)
#    self.z = rad * np.sin(ls) + alt * np.sin(lat)
#    p = Proj(proj='utm',ellps='WGS84')
#    self.x, self.y = p(lat, lon)
    coord = utm.from_latlon(lat, lon)
    self.x = coord[0]
    self.y = coord[1]
    self.z = alt
#    print coord, coord[0]
#    self.x, self.y = utm.from_latlon

#    rad = np.float64(6378137.0)        # Radius of the Earth (in meters)
#    f = np.float64(1.0/298.257223563)  # Flattening factor WGS84 Model
#    cosLat = np.cos(lat)
#    sinLat = np.sin(lat)
#    FF     = (1.0-f)**2
#    C      = 1/np.sqrt(cosLat**2 + FF * sinLat**2)
#    S      = C * FF
#
#    self.x = 5175000 - (rad * C + alt) * cosLat * np.cos(lon)
#    self.y = 2555900 + (rad * C + alt) * cosLat * np.sin(lon)
#    self.z = 2704400 - (rad * S + alt) * sinLat
#    self.z = alt

  def recalculeCoordonneesCartesiennes(self):
#    lat = self.theta
#    lon = self.phi
#    alt = self.r
#    
#    flat = 1/298.257223563
#    ls = np.arctan((1 - flat)**2 * np.tan(lat))
#    rad = np.sqrt(6378137 **2 /(1 +(1/((1-flat)**2-1))*np.sin(ls)**2))
#    x = rad * np.cos(ls) * np.cos(lon) + alt * np.cos(lat) * np.cos(lon)
#    y = rad * np.cos(ls) * np.sin(lon) + alt * np.cos(lat) * np.sin(lon)
#    z = rad * np.sin(ls) + alt * np.sin(lat)

    self.x = self.r * np.sin(self.theta) * np.cos(self.phi);
    self.y = self.r * np.sin(self.theta) * np.sin(self.theta);
    self.z = self.r * np.cos(self.theta);

  def getCosTheta(self):
    self.r = np.sqrt(self.x * self.x + self.y * self.y + self.z * self.z)
    return self.z / self.r
    
  def recalculeCoordonneesSpheriques(self):
      self.r = np.sqrt(self.x * self.x + self.y * self.y + self.z * self.z)

      if self.x == 0 and self.y == 0:
        self.theta = 0;
        self.phi = 0;
      else:
        if (self.r > 0) :
          self.theta = np.arccos(self.z / self.r);
          self.phi = np.arctan2(self.y, self.x);
        else:
          self.theta = 0;
          self.phi = 0;

  def normaliser(self):
    self.r = np.sqrt(self.x * self.x + self.y * self.y + self.z * self.z)
    self.x /= self.r
    self.y /= self.r
    self.z /= self.r

  def calculPointArrivee(self, direction, longueur):
    arrivee = Point3D()
    arrivee.x = self.x + direction.x * longueur
    arrivee.y = self.y + direction.y * longueur
    arrivee.z = self.z + direction.z * longueur
#    arrivee.recalculeCoordonneesSpheriques()
    return arrivee

  def moins(self, point2):
    x = self.x - point2.x
    y = self.y - point2.y
    z = self.z - point2.z
    return Point3D(x, y, z)
    
  def getDistance(self, point2):
    return np.sqrt((self.x - point2.x)**2 + (self.y - point2.y)**2 + (self.z - point2.z)**2)
  
  def getDistanceHorizontale(self, point2):
    return np.sqrt((self.x - point2.x)**2 + (self.y - point2.y)**2)

  def estInferieurA(self, point):
    return self.x < point.x and self.y < point.y and self.z < point.z
  def estSuperieurA(self, point):
    return self.x > point.x and self.y > point.y and self.z > point.z

  def miseAJourDuMin(self, pointAcomparer):
    if self.x > pointAcomparer.x:
      self.x = pointAcomparer.x

    if self.y > pointAcomparer.y:
      self.y = pointAcomparer.y
      
    if self.y > pointAcomparer.z:
      self.y = pointAcomparer.z


class Visualisateur():
  def __init__(self):
    self.proportionPtsAfficher = 10

  def afficheFichierPointsExtraits(self, cheminFichier, sauver = False, typeVisu = "intensite", afficheProjection = False, **kwargs):
    print "chargement ", cheminFichier, 
    fic = open(cheminFichier, "r")
    intensiteMax = 1000000000
    intensiteMin = -100000
    deviationMax = 1000000000
    deviationMin = -1200000
    zMax = 10000
    zMin = -10000
    if "iMax" in kwargs:
      intensiteMax = float(kwargs["iMax"])
    if "iMin" in kwargs:
      intensiteMin = float(kwargs["iMin"])
    if "dMax" in kwargs:
      deviationMax = float(kwargs["dMax"])
    if "dMin" in kwargs:
      deviationMin = float(kwargs["dMin"])
    if "zMax" in kwargs:
      zMax = float(kwargs["zMax"])
    if "zMin" in kwargs:
      zMin = float(kwargs["zMin"])
    if "show" in kwargs:
      show = kwargs["show"]
    else:
      show = True


    matriceTransformation = MatriceTransformation()
    X, Y, Z, I, J, K = [], [], [], [], [], []
    X2, Y2, Z2, I2, J2, K2 = [], [], [], [], [], []
    nbPointsTotal = 0
    dist = 30
    limiteNbPoints = 1000000
    longueur = 6
    for i, ligne in enumerate(fic):
      lig = map(float, ligne.rstrip('\n\r').split())
      if i == 0:
        matriceTransformation.setMatrice(lig)
        pointDepart = matriceTransformation.translation(Point3D(0, 0, 0))
        #print matriceTransformation.getMatriceString(), pointDepart
#         if typeVisu == "nbEchos":
#           X.append(pointDepart.x)
#           Y.append(pointDepart.y)    
#           Z.append(pointDepart.z)  
#           I.append(5)
      else:
        if i == 1:
          longueur = len(lig)
          if lig[0] == 2:
            longueur = 4
          print "longueur", longueur
        if self.proportionPtsAfficher == 0 or i % self.proportionPtsAfficher == 0:
          nbPointsTotal += 1
          if nbPointsTotal < limiteNbPoints:
            if longueur == 6:
              point = Point3D(lig[0], lig[1], lig[2])
              if not matriceTransformation.isMatriceUnite():
                point = matriceTransformation.transformation(point)
#              print lig[4], intensiteMin, intensiteMax, (intensiteMin <= lig[4] <= intensiteMax)
              if intensiteMin <= lig[4] <= intensiteMax and deviationMin <= lig[5] <= deviationMax and zMin <= point.z <= zMax:
                X.append(point.x)
                Y.append(point.y)
                Z.append(point.z)
                I.append(lig[4])
                J.append(lig[5])
                K.append(lig[3])
              else:
                X2.append(point.x)
                Y2.append(point.y)
                Z2.append(point.z)
                I2.append(lig[4])
                J2.append(lig[5])
                K2.append(lig[3])
            else:
              nbPoints = int(lig[0])
              nbPointsTotal += nbPoints
              dir = Point3D(lig[1], lig[2], lig[3])
              if not matriceTransformation.isMatriceUnite():
                dir = matriceTransformation.rotation(dir)
              if afficheProjection:
                nouveauPoint = pointDepart.calculPointArrivee(dir, dist)
                X.append(nouveauPoint.x)
                Y.append(nouveauPoint.y)
                Z.append(nouveauPoint.z)
                I.append(nbPoints + 1)
              for n in range(nbPoints):
                if len(lig) < 4 + n:
                  print "erreur ligne ", i, lig
                else:
                  distance = lig[4 + n]
                  nouveauPoint = pointDepart.calculPointArrivee(dir, distance)
                  X.append(nouveauPoint.x)
                  Y.append(nouveauPoint.y)
                  Z.append(nouveauPoint.z)
                  I.append(n)
          else:
            print "limite du nombre de points à afficher atteint"
            break

    print nbPointsTotal, "fin chargement ", cheminFichier, afficheProjection
#    print I
    if sauver:
      cheminFichier3D = cheminFichier.replace(".txt", ".png")
    else:
      cheminFichier3D = ""
    if typeVisu == "intensite":
      afficheXYZI(X, Y, Z, I, 1, cheminFichier3D, titre = "Intensite", _show = show)
      if X2:
        afficheXYZI(X2, Y2, Z2, I2, 1, cheminFichier3D, titre = "Intensite", _show = True)
    elif typeVisu == "deviation":
      afficheXYZI(X, Y, Z, J, 1, cheminFichier3D, titre = "Deviation", _show = show)
      if X2:
        afficheXYZI(X2, Y2, Z2, J2, 1, cheminFichier3D, titre = "Deviation", _show = True)
    elif typeVisu == "nbEchos":
      afficheXYZI(X, Y, Z, I, 1, cheminFichier3D, titre = "number of echoes", _show = show)
    else:
      afficheXYZI(X, Y, Z, K, 1, cheminFichier3D, titre = "Ordre", _show = show)
      if X2:
        afficheXYZI(X2, Y2, Z2, K2, 1, cheminFichier3D, titre = "Ordre", _show = True)

    return 0


  def afficheFichierPointsExtraitsEtMaillage(self, maillage, R, cheminFichier, sauver = False, typeVisu = "intensite", afficheProjection = False, **kwargs):
    print "chargement ", cheminFichier
    fic = open(cheminFichier, "r")
    intensiteMax = 1000000000
    intensiteMin = -100000
    deviationMax = 1000000000
    deviationMin = -1200000
    zMax = 10000
    zMin = -10000
    if "iMax" in kwargs:
      intensiteMax = float(kwargs["iMax"])
    if "iMin" in kwargs:
      intensiteMin = float(kwargs["iMin"])
    if "dMax" in kwargs:
      deviationMax = float(kwargs["dMax"])
    if "dMin" in kwargs:
      deviationMin = float(kwargs["dMin"])
    if "zMax" in kwargs:
      zMax = float(kwargs["zMax"])
    if "zMin" in kwargs:
      zMin = float(kwargs["zMin"])
    if "show" in kwargs:
      show = kwargs["show"]
    else:
      show = True


    matriceTransformation = MatriceTransformation()
    X, Y, Z, I, J, K = [], [], [], [], [], []
    X2, Y2, Z2, I2, J2, K2 = [], [], [], [], [], []
    nbPointsTotal = 0
    dist = 40
    limiteNbPoints = 1000000
    longueur = 6
    for i, ligne in enumerate(fic):
      lig = map(float, ligne.rstrip('\n\r').split())
      if i == 0:
        matriceTransformation.setMatrice(lig)
        pointDepart = matriceTransformation.translation(Point3D(0, 0, 0))
        #print matriceTransformation.getMatriceString(), pointDepart
        if typeVisu == "nbEchos":
          X.append(pointDepart.x)
          Y.append(pointDepart.y)    
          Z.append(pointDepart.z)  
          I.append(5)
      else:
        if i == 1:
          longueur = len(lig)
          if lig[0] == 2:
            longueur = 4
          print "longueur", longueur
        if self.proportionPtsAfficher == 0 or i % self.proportionPtsAfficher == 0:
          nbPointsTotal += 1
          if nbPointsTotal < limiteNbPoints:
            if longueur == 6:
              point = Point3D(lig[0], lig[1], lig[2])
              if not matriceTransformation.isMatriceUnite():
                point = matriceTransformation.transformation(point)
#              print lig[4], intensiteMin, intensiteMax, (intensiteMin <= lig[4] <= intensiteMax)
              if intensiteMin <= lig[4] <= intensiteMax and deviationMin <= lig[5] <= deviationMax and zMin <= point.z <= zMax:
                X.append(point.x)
                Y.append(point.y)
                Z.append(point.z)
                I.append(lig[4])
                J.append(lig[5])
                K.append(lig[3])
              else:
                X2.append(point.x)
                Y2.append(point.y)
                Z2.append(point.z)
                I2.append(lig[4])
                J2.append(lig[5])
                K2.append(lig[3])
            else:
              nbPoints = int(lig[0])
              nbPointsTotal += nbPoints
              dir = Point3D(lig[1], lig[2], lig[3])
              if not matriceTransformation.isMatriceUnite():
                dir = matriceTransformation.rotation(dir)
              if afficheProjection:
                nouveauPoint = pointDepart.calculPointArrivee(dir, dist)
                X.append(nouveauPoint.x)
                Y.append(nouveauPoint.y)
                Z.append(nouveauPoint.z)
                I.append(nbPoints + 1)
              for n in range(nbPoints):
                if len(lig) < 4 + n:
                  print "erreur ligne ", i, lig
                else:
                  distance = lig[4 + n]
                  nouveauPoint = pointDepart.calculPointArrivee(dir, distance)
                  X.append(nouveauPoint.x)
                  Y.append(nouveauPoint.y)
                  Z.append(nouveauPoint.z)
                  I.append(n)
          else:
            print "limite du nombre de points à afficher atteint"
            break

          from mayavi import mlab
    fig = mlab.figure()
    fig.scene.disable_render = True
    pts = mlab.points3d(X, Y, Z, I, colormap = "jet", mode = 'point', scale_factor = 1)
    pts.glyph.scale_mode = 'data_scaling_off'
    
    
    X, Y, Z, I, = [], [], [], []
    for indice in maillage.listeVoxels.keys():
      rho = maillage.getDensite(indice)
#      print indice, rho
      if ~ np.isnan(rho) and rho > 0:
        X.append(indice[0] * R)
        Y.append(indice[1] * R)
        Z.append(indice[2] * R)
        I.append(rho)
    
    pts2 = mlab.points3d(X, Y, Z, I, colormap = "jet", mode = 'cube', scale_factor = 0.5 * R)
    pts2.glyph.scale_mode = 'data_scaling_off'

  
    mlab.axes()
  #  mlab.orientation_axes()
  #  f = mlab.gcf()
  #  camera = f.scene.camera
  #  print mlab.view()
  #  mlab.view(azimuth=45, elevation=6max, distance=15, focalpoint=(maxX,0,0))
  
    fig.scene.disable_render = False
    mlab.show()
    return 0
  
  def visualiserFichierPointsALS(self, cheminFic, sauver = False):
    matriceTransformation = MatriceTransformation()
    fic = open(cheminFic, "r")
    print cheminFic
    X, Y, Z, I, J, K = [], [], [], [], [], []
    nbPointsTotal = 0
    limiteNbPoints = 1000000
    longueur = 6
    matriceUnite = True
    for i, ligne in enumerate(fic):
      if i == 0:
        try:
          lig = map(float, ligne.rstrip('\n\r').split())
          matriceTransformation.setMatrice(lig)
        except:
          matriceTransformation = MatriceTransformation()

        print matriceTransformation.getMatriceString()
#        X.append(pointDepart.x)
#        Y.append(pointDepart.y)    
#        Z.append(pointDepart.z)  
#        I.append(1)
#        J.append(1)
#        K.append(1)
      else:
        lig = map(float, ligne.rstrip('\n\r').split())
        if self.proportionPtsAfficher == 0 or i % self.proportionPtsAfficher == 0:
          nbPointsTotal += 1
          if nbPointsTotal < limiteNbPoints:
            nbEcho = int(lig[0])
            if not matriceTransformation.isMatriceUnite():
              dir = matriceTransformation.rotation(Point3D(lig[4], lig[5], lig[6]))
              pointDepart = matriceTransformation.transformation(Point3D(lig[1], lig[2], lig[3]))
            else:
              pointDepart = Point3D(lig[1], lig[2], lig[3])
              dir = Point3D(lig[4], lig[5], lig[6])
            X.append(pointDepart.x)
            Y.append(pointDepart.y)
            Z.append(pointDepart.z)
            I.append(1)
            for numEcho in range(nbEcho):
              pointArrivee = pointDepart.calculPointArrivee(dir, lig[7 + numEcho])
              X.append(pointArrivee.x)
              Y.append(pointArrivee.y)
              Z.append(pointArrivee.z)
              I.append(5)
#              print lig[4], intensiteMin, intensiteMax, (intensiteMin <= lig[4] <= intensiteMax)
#            if intensiteMin <= lig[4] <= intensiteMax and deviationMin <= lig[5] <= deviationMax and zMin <= point.z <= zMax:


          else:
            print "limite du nombre de points à afficher atteint"
            break

    print nbPointsTotal, "fin chargement ", cheminFic

    if sauver:
      cheminFichier3D = cheminFic.replace(".txt", ".png")
    else:
      cheminFichier3D = ""
    afficheXYZI(X, Y, Z, I, 1, cheminFichier3D, titre = u"Intensite")


  def affichePointsLAS(self, cheminLas):
    from liblas import file
    f = file.File(cheminLas, mode = 'r')
    X, Y, Z, I = [], [], [], []

    limiteNbPoints = 1000000
    for i, p in enumerate(f):
      pointCourant = Point3D(p.x, p.y, p.z)
      if i > limiteNbPoints:
        break
      X.append(p.x)
      Y.append(p.y)
      Z.append(p.z)
      I.append(p.intensity)

    print "Nombre de points affiches :", len(X), "(ficher : ", cheminLas, ")"

    afficheXYZI(X, Y, Z, I, 1, mode = 'point', titre = u"Intensite")


  def affichePointsLASDansZone(self, cheminLas, limiteMin = Point3D(-1000, -1000, -1000), limiteMax = Point3D(1000, 1000, 1000)):
    from liblas import file
    f = file.File(cheminLas, mode = 'r')
    X, Y, Z, I = [], [], [], []

    def isPointDansMaille(point):
      return point[0] >= limiteMin.x and point[1] >= limiteMin.y and point[2] >= limiteMin.z and point[0] < limiteMax.x and point[1] < limiteMax.y and point[2] < limiteMax.z


    limiteNbPoints = 1000000
    for i, p in enumerate(f):
      pointCourant = Point3D(p.x, p.y, p.z)
      if i > limiteNbPoints:
        break
      if i % self.proportionPtsAfficher == 0 and isPointDansMaille(pointCourant):
        X.append(p.x)
        Y.append(p.y)
        Z.append(p.z)
        I.append(p.intensity)

    afficheXYZI(X, Y, Z, I, 1, mode = 'cube', titre = u"Intensite")

def fusionnerFichiersPoints(listeFichiers):
  print "fusion des ficheirs points : ", listeFichiers
  if len(listeFichiers) < 2:
    return

  if os.path.isfile(listeFichiers[0] + "_fusion.txt"):
    print "fichier deja calculé:", listeFichiers[0] + "_fusion.txt"
    return listeFichiers[0] + "_fusion.txt"
  
  ficOut = open(listeFichiers[0] + "_fusion.txt", "w")
  ficOut.write("1 0 0 0 0 1 0 0 0 0 1 0 0 0 0 1\n")
  transformeCoord = True
  for fichier in listeFichiers:
    print "fusion fichier :" , fichier
    ficIn = open(fichier, "r")
    matriceTransformation = MatriceTransformation()
    for i, ligne in enumerate(ficIn):
      lig = map(float, ligne.rstrip('\n\r').split())
      if i == 0:
        matriceTransformation.setMatrice(lig)
        pointDepart = matriceTransformation.translation(Point3D(0, 0, 0))
        if matriceTransformation.isIdentite():
          transformeCoord = False
      else:
        if i == 1:
          longueur = len(lig)
          if lig[0] == 2:
            longueur = 4
        else:
          if longueur == 6:
            if transformeCoord:
              point = Point3D(lig[1], lig[2], lig[3])
              if transformeCoord:
                point = matriceTransformation.transformation(point)
              ficOut.write("%f %f %f %d %f %f\n" % (point.x, point.y, point.z, lig[4], lig[5], lig[6]))
            else:
              ficOut.write(ligne)

          else:
            nbPoints = int(lig[0])
            dir = matriceTransformation.transformation(Point3D(lig[1], lig[2], lig[3]))
            for n in range(nbPoints):
              if len(lig) < 4 + n:
                print "erreur ligne ", i, lig
              else:
                distance = lig[4 + n]
                nouveauPoint = pointDepart.calculPointArrivee(dir, distance)
                ficOut.write("%f %f %f %d %f %f\n" % (nouveauPoint.x, nouveauPoint.y, nouveauPoint.z, n, 1, 1))

    ficIn.close()
  return listeFichiers[0] + "_fusion.txt"




def afficheCourbe3D(X, Y, Z, I, titre = ""):
  from mayavi import mlab
  mlab.figure()
  mlab.plot3d(X, Y, Z, I)
  mlab.colorbar(title = titre)#title
  #mlab.colorbar(orientation = 'vertical', title = titre)#title
  mlab.show()

def afficheSurface3D(x, y, z, titre = None):
  from mayavi import mlab
  mlab.figure()
  #mlab.imshow(z, interpolate = False)
  mlab.surf(x, y, z, warp_scale = 'auto')

  mlab.colorbar(title = titre)#title
  #mlab.colorbar(orientation = 'vertical', title = titre)#title
  mlab.show()

def afficheXYZI(X, Y, Z, I, resolution = 1, chemin = "", mode = 'point', titre = '', afficheCube = False, minCube = [0, 0, 1], maxCube = [5, 5, 6], _show = True):
  if len(X) == 0:
    print "pas de points à afficher..."
    return

  from mayavi import mlab
  fig = mlab.figure()
  fig.scene.disable_render = True
  if mode == 'point':
    pts = mlab.points3d(X, Y, Z, I, colormap = "jet", mode = 'point', scale_factor = 1)
  else:
    pts = mlab.points3d(X, Y, Z, I, colormap = "jet", mode = 'cube', scale_factor = 0.5 * resolution)

#   pts.glyph.scale_mode = 'data_scaling_off'
  mlab.colorbar(title = titre)#title
  mlab.colorbar(orientation = 'vertical', title = titre)#title

  if afficheCube:
    minZ = minCube[2]
    maxZ = maxCube[2]
    minX = minCube[0]
    maxX = maxCube[0]
    minY = minCube[1]
    maxY = maxCube[1]
    x = [minX, minX, minX, minX, minX, maxX, maxX, maxX, maxX, maxX, minX, maxX, minX, minX, minX, maxX, maxX, minX]
    y = [minY, minY, maxY, maxY, minY, minY, minY, maxY, maxY, minY, minY, minY, minY, maxY, maxY, maxY, maxY, maxY]
    z = [min, max, max, min, min, min, max, max, min, min, max, max, min, min, max, max, min, min]
    s = mlab.plot3d(x, y, z)


  #mlab.axes()
  
  mlab.orientation_axes()
#  f = mlab.gcf()
#  camera = f.scene.camera
#  print mlab.view()
#  mlab.view(azimuth=45, elevation=6max, distance=15, focalpoint=(maxX,0,0))

  fig.scene.disable_render = False
  if chemin != "":
    print "sauvegarde fichier ", chemin
    mlab.savefig(chemin)
  if _show:
    mlab.show()
  elif chemin != "":
    mlab.close()

def transformeFichierPointsEnFichierEntreeVoxelisation(cheminFichierIn, cheminFichierOut):
  ficIn = open(cheminFichierIn, "r")
  ficOut = open(cheminFichierOut, "w")
  ficOut.write("1 0 0 0 0 1 0 0 0 0 1 0 0 0 0 1\n")
  pointDepart = Point3D(0, 0, 0)
  for i, lig in enumerate(ficIn.readlines()):
    if len(lig.rstrip().split()) > 2:
      ligne = map(float, lig.rstrip().split())
      point = Point3D(ligne[0], ligne[1], ligne[2])
      dir = point.moins(pointDepart)
      dir.normaliser()
      ficOut.write("1 %f %f %f %f\n" %(dir.x, dir.y, dir.z, point.getDistance(pointDepart)))
    else:
      print "ligne non correcte",  i, lig
    
    
#def afficheFichierPoints(cheminFichier, sauver = False):
#  print "chargement ", cheminFichier
#  fic = open(cheminFichier, "r")
#  lignes = fic.readlines()
#  fic.close()
#
#  matriceTransformation = MatriceTransformation()
#  X, Y, Z, I = [], [], [], []
#  nbPointsTotal, i = 0, 0
#  dist = 25
#  limiteNbPoints = 3000000
#  for ligne in lignes:
#    lig = map(float, ligne.rstrip('\n\r').split())
#    if nbPointsTotal < limiteNbPoints:
#      if i == 0:
#        matriceTransformation.setMatrice(lig)
#        pointDepart = matriceTransformation.translation(Point3D(0, 0, 0))
#        print matriceTransformation.getMatriceString(), pointDepart
#        X.append(pointDepart.x)
#        Y.append(pointDepart.y)
#        Z.append(pointDepart.z)
#        I.append(5)
#      else:
#        nbPoints = int(lig[0])
#        nbPointsTotal += nbPoints
#        dir = Point3D(lig[1], lig[2], lig[3])
#        nouveauPoint = pointDepart.calculPointArrivee(dir, dist)
#        X.append(nouveauPoint.x)
#        Y.append(nouveauPoint.y)
#        Z.append(nouveauPoint.z)
#        I.append(nbPoints + 1)
#        for n in range(nbPoints):
#          if len(lig) < 4 + n:
#            print "erreur ligne ", i, lig
#          else:
#            distance = lig[4 + n]
#            if distance < dist:
#              nouveauPoint = pointDepart.calculPointArrivee(dir, distance)
#              X.append(nouveauPoint.x)
#              Y.append(nouveauPoint.y)
#              Z.append(nouveauPoint.z)
#              I.append(n)
#    else:
#      break
#    i += 1
#
#  print len(lignes), nbPointsTotal, "fin chargement ", cheminFichier
#
#  if sauver:
#    cheminFichier3D = cheminFichier.replace(".txt", ".png")
#  else:
#    cheminFichier3D = ""
#  afficheXYZI(X, Y, Z, I, 1, cheminFichier3D, titre = "number of echoes")


def calculerNinterceptesDansvoxels(cheminFichier, limitesMin, limiteMax, R):

  nbMailles = (limiteMax - limitesMin)
  nbMailles.diviserVal(R)
  nbMailles.valeurEntiere()
  print nbMailles

  def getIndice(position):
    return (int((position.x - limitesMin.x) / R), int((position.y - limitesMin.y) / R), int((position.z - limitesMin.z) / R))

  def isIndiceDansMaille(indices):
    return indices[0] >= 0 and indices[1] >= 0 and indices[2] >= 0 and indices[0] < nbMailles.x and indices[1] < nbMailles.y and indices[2] < nbMailles.z

  def isPointDansMaille(point):
    return point[0] >= limitesMin.x and point[1] >= limitesMin.y and point[2] >= limitesMin.z and point[0] < limiteMax.x and point[1] < limiteMax.y and point[2] < limiteMax.z

  fic = open(cheminFichier, "r")
  lignes = fic.readlines()
  fic.close()

  matriceTransformation = MatriceTransformation()
  X, Y, Z, I = [], [], [], []
  nbPointsTotal, i = 0, 0
  maille = {}
  transformeCoord = True
  for i, ligne in enumerate(lignes):
    lig = map(float, ligne.rstrip('\n\r').split())
    if i == 0:
      matriceTransformation.setMatrice(lig)
      pointDepart = matriceTransformation.translation(Point3D(0, 0, 0))
      if matriceTransformation.isIdentite():
        transformeCoord = False
      print matriceTransformation.getMatriceString(), pointDepart
    else:
      if i == 1:
        longueur = len(lig)
        if lig[0] == 2:
          longueur = 4
      else:
        if longueur == 6:
          point = Point3D(lig[1], lig[2], lig[3])
          if transformeCoord:
            point = matriceTransformation.transformation(point)

          if isPointDansMaille(point):
            indice = getIndice(point)
            if indice in maille:
              maille[indice] += 1
            else:
              maille[indice] = 1
            print point, isPointDansMaille(point), indice, maille[indice]

        else:
          nbPoints = int(lig[0])
          nbPointsTotal += nbPoints
          dir = Point3D(lig[1], lig[2], lig[3])
          if transformeCoord:
            dir = matriceTransformation.rotation(dir)

          for n in range(nbPoints):
            if len(lig) < 4 + n:
              print "erreur ligne ", i, lig
            else:
              point = pointDepart.calculPointArrivee(dir, lig[4 + n])
              if isPointDansMaille(point):
                indice = getIndice(point)

                if indice in maille:
                  maille[indice] += 1
                else:
                  maille[indice] = 1

  ficOut = open("/homeL/grau/tmp/testVoxels.txt", "w")
  for k in range(nbMailles.z):
    for i in range(nbMailles.x):
      for j in range(nbMailles.y):
        if (i, j, k) in maille:
          ficOut.write("%d %d %d %d\n" % (i, j, k, maille[(i, j, k)]))
        else:
          ficOut.write("%d %d %d 0\n" % (i, j, k))

#        print i, j, k, maille[(i, j, k)]

#
#def afficheFichierPointsExtraits(cheminFichier, sauver = False, typeVisu = "intensite", **kwargs):
#  print "chargement ", cheminFichier
#  fic = open(cheminFichier, "r")
#  lignes = fic.readlines()
#  fic.close()
#  intensiteMax = 1000000000
#  intensiteMin = 0
#  deviationMax = 1000000000
#  deviationMin = 0
#  zMax = 10000
#  zMin = 0
#  if "iMax" in kwargs:
#    intensiteMax = float(kwargs["iMax"])
#  if "iMin" in kwargs:
#    intensiteMin = float(kwargs["iMin"])
#  if "dMax" in kwargs:
#    deviationMax = float(kwargs["dMax"])
#  if "dMin" in kwargs:
#    deviationMin = float(kwargs["dMin"])
#  if "zMax" in kwargs:
#    zMax = float(kwargs["zMax"])
#  if "zMin" in kwargs:
#    zMin = float(kwargs["zMin"])
#
#  matriceTransformation = MatriceTransformation()
#  X, Y, Z, I, J, K = [], [], [], [], [], []
#  nbPointsTotal = 0
#  dist = 25
#  limiteNbPoints = 3000000
#  longueur = 6
#  for i, ligne in enumerate(lignes):
#    lig = map(float, ligne.rstrip('\n\r').split())
#    if nbPointsTotal < limiteNbPoints:
#      if i == 0:
#        matriceTransformation.setMatrice(lig)
#        pointDepart = matriceTransformation.translation(Point3D(0, 0, 0))
#        print matriceTransformation.getMatriceString(), pointDepart
##        X.append(pointDepart.x)
##        Y.append(pointDepart.y)    
##        Z.append(pointDepart.z)  
##        I.append(1)
##        J.append(1)
##        K.append(1)
#      else:
#        if i == 1:
#          longueur = len(lig)
#          print longueur
#        nbPointsTotal += 1
#        if longueur == 6:
#          point = Point3D(lig[0], lig[1], lig[2])
#
#          if intensiteMin <= lig[4] <= intensiteMax and deviationMin <= lig[5] <= deviationMax and zMin <= point.z <= zMax:
#            X.append(point.x)
#            Y.append(point.y)
#            Z.append(point.z)
#            I.append(lig[4])
#            J.append(lig[5])
#            K.append(lig[3])
#        else:
#          nbPoints = int(lig[0])
#          nbPointsTotal += nbPoints
#          dir = Point3D(lig[1], lig[2], lig[3])
#          nouveauPoint = pointDepart.calculPointArrivee(dir, dist)
#          X.append(nouveauPoint.x)
#          Y.append(nouveauPoint.y)
#          Z.append(nouveauPoint.z)
#          I.append(nbPoints + 1)
#          for n in range(nbPoints):
#            if len(lig) < 4 + n:
#              print "erreur ligne ", i, lig
#            else:
#              distance = lig[4 + n]
#              if distance < dist and n == 0:
#                nouveauPoint = pointDepart.calculPointArrivee(dir, distance)
#                X.append(nouveauPoint.x)
#                Y.append(nouveauPoint.y)
#                Z.append(nouveauPoint.z)
#                I.append(n)
#    else:
#      print "limite du nombre de points à afficher atteint"
#      break
#
#  print len(lignes), nbPointsTotal, "fin chargement ", cheminFichier
#
#  if sauver:
#    cheminFichier3D = cheminFichier.replace(".txt", ".png")
#  else:
#    cheminFichier3D = ""
#  if typeVisu == "intensite":
#    afficheXYZI(X, Y, Z, I, 1, cheminFichier3D, mode = 'cube', titre = "Intensite")
#  elif typeVisu == "deviation":
#    afficheXYZI(X, Y, Z, J, 1, cheminFichier3D, titre = "Deviation")
#  else:
#    afficheXYZI(X, Y, Z, K, 1, cheminFichier3D, titre = "Ordre")
#
#  return 0
##  arr = mlab.screenshot()
##  import pylab as pl
##  pl.imshow(arr)
##  pl.axis('off')
##  pl.show()


def filtrerFichierPoints(cheminFichier, **kwargs):
  fic = open(cheminFichier, "r")
  lignes = fic.readlines()
  fic.close()

  fic = open(cheminFichier + "_filtre.txt", "w")

  iMax = 10000
  iMin = 0
  dMax = 10000
  dMin = 0
  zMax = 10000
  zMin = 0
  if "iMax" in kwargs:
    intensiteMax = float(kwargs["iMax"])
  if "iMin" in kwargs:
    intensiteMin = float(kwargs["iMin"])
  if "dMax" in kwargs:
    deviationMax = float(kwargs["dMax"])
  if "dMin" in kwargs:
    deviationMin = float(kwargs["dMin"])
  if "zMax" in kwargs:
    zMax = float(kwargs["zMax"])
  if "zMin" in kwargs:
    zMin = float(kwargs["zMin"])

  print kwargs
  print "filtrage...",
  print kwargs


  for i, ligne in enumerate(lignes):
    if i == 0:
      fic.write(ligne)
    else:
      lig = map(float, ligne.rstrip('\n\r').split())
      if i == 1:
        longueur = len(lig)
#      print longueur, lig, intensiteMin, intensiteMax, deviationMin, deviationMax, zMin, zMax
#      print lig[4], lig[5], lig[2], (intensiteMin <= lig[4] <= intensiteMax ), (deviationMin <= lig[5] <= deviationMax), (zMin <= lig[2] <= zMax)
      if longueur == 6:
        if intensiteMin <= lig[4] <= intensiteMax and deviationMin <= lig[5] <= deviationMax and zMin <= lig[2] <= zMax:
          fic.write(ligne)
      else:
        print "Format du fichier ", cheminFichier, " non  pris en charge... "
        return 0

  print "Fin filtrage..."


def lireMNT(fichierMNT):
    fic = open(fichierMNT, "r")
    lignes = fic.readlines()
    premiereLigne = lignes[0].rstrip().split(" ")
    print "premiereLigne" , premiereLigne
    R = 1
    if len(premiereLigne) == 2:
      typeFic = "entete"
      nbX = int(premiereLigne[0])
      nbY = int(premiereLigne[1])
    elif len(premiereLigne) == 3:
      typeFic = "entete"
      nbX = int(premiereLigne[0])
      nbY = int(premiereLigne[1])
      R = float(premiereLigne[2])
    else:
      typeFic = "normal"


    xi, yi, zi = [], [], []

    for i, lig in enumerate(lignes):
      if typeFic != "entete" or i > 0:
        li = map(float, lig.rstrip().split())
        xi.append(li[0])
        yi.append(li[1])
        zi.append(li[2])

    if typeFic == "entete":
      xi = np.array(xi).reshape((nbX, nbY))
      yi = np.array(yi).reshape((nbX, nbY))
      zi = np.array(zi).reshape((nbX, nbY))

    return xi, yi, zi, R


def filtrerFichierPointsSurCouchedZAvecMNT(fichierPoint, altitudeMoy, deltaZ, fichierMNT = ""):
  ficPoints = open(fichierPoint, "r")
  
  if os.path.isfile(fichierPoint + "_filtreDz.txt"):
    print "fichier deja calculé:",fichierPoint + "_filtreDz.txt"
    return fichierPoint + "_filtreDz.txt"
  
  ficOut = open(fichierPoint + "_filtreDz.txt", "w")

  print "Filtrage des points - ", fichierPoint, altitudeMoy, deltaZ, fichierMNT
  if fichierMNT:
    xi, yi, zi, R = lireMNT(fichierMNT)


  def getZMNTPourPosition(x, y):
    if fichierMNT:
      return zi[int(x / R)][int(y / R)]
    return 0

  def pointDansSousCouche(z):
    return  np.abs(z - altitudeMoy) < deltaZ / 2

  transformeCoord = True
  matriceTransformation = MatriceTransformation()
  for i, ligne in enumerate(ficPoints):
    lig = map(float, ligne.rstrip('\n\r').split())
    if i == 0:
        matriceTransformation.setMatrice(lig)
        pointDepart = matriceTransformation.translation(Point3D(0, 0, 0))
        if matriceTransformation.isIdentite():
          transformeCoord = False

        ficOut.write(ligne)
    else:
      if i == 1:
        longueur = len(lig)
      if longueur == 6:
        point = Point3D(lig[0], lig[1], lig[2])
        if transformeCoord:
          point = matriceTransformation.transformation(point)
        point.z -= getZMNTPourPosition(point.x, point.y)
        if  pointDansSousCouche(point.z):
          ficOut.write("%f %f %f %d %f %f\n" % (point.x, point.y, point.z, int(lig[3]), lig[4], lig[5]))
      else:
        print "Format du fichier ", cheminFichier, " non  pris en charge..."
        return 0
  return fichierPoint + "_filtreDz.txt"




def projeterFichierPoints(kwargs, cheminImage = ""):
  fic = open(kwargs["cheminFichier"], "r")
  lignes = fic.readlines()
  fic.close()

  R = float(kwargs["resolution"])

  cheminImage = kwargs["cheminFichier"] + "_projection_" + str(R) + ".png"
  #ficOut = open(kwargs["cheminFichier"] +"_projection_"+str(R)+".txt", "w")

  pointMin = (float(kwargs["pointMin_x"]), float(kwargs["pointMin_y"]))
  pointMax = (float(kwargs["pointMax_x"]), float(kwargs["pointMax_y"]))

  nbX = int((pointMax[0] - pointMin[0] + 1) / R)
  nbY = int((pointMax[1] - pointMin[1] + 1) / R)

  x = np.linspace(pointMin[0], pointMax[0], nbX)
  y = np.linspace(pointMin[1], pointMax[1], nbY)

#  image2D_x, image2D_y = np.mgrid[pointMin[0]:pointMax[0]:nbX, pointMin[1]:pointMax[1]:nbY]

#  print image2D_x
#  print np.shape(image2D_x)

  image = np.zeros((nbX, nbY))
  print "projection image", kwargs

  matriceTransformation = MatriceTransformation()
  for i, ligne in enumerate(lignes):
    lig = map(float, ligne.rstrip('\n\r').split())
    if i == 0:
        matriceTransformation.setMatrice(lig)
        pointDepart = matriceTransformation.translation(Point3D(0, 0, 0))
    else:
      longueur = len(lig)
      if longueur == 6:
        point = Point3D(lig[0], lig[1], lig[2])
        indiceX = int((lig[0] - pointMin[0]) / R)
        indiceY = int((lig[1] - pointMin[1]) / R)
#        print R, point, indiceX, indiceY, (lig[0] + pointMin[0]) /  R
        if 0 <= indiceX < nbX and 0 <= indiceY < nbY:
          image[indiceX, indiceY] += 1
          #print indiceX, indiceY
          #image2D[indiceX][indiceY] += 1


  plt.imshow(image, cmap = "jet", interpolation = 'nearest', aspect = 'equal', origin = "lower")
#  plt.colorbar()
  plt.axis('off')
  plt.subplots_adjust(0, 0, 1, 1, 0, 0)
  if cheminImage:
    plt.savefig(cheminImage, bbox_inches = 'tight')
  plt.show()

#  afficheSurface3D(image2D_x, image2D_y, image)



  return 0
#  for i in range(len(grille2D[0])
#
#  for i, ligne in enumerate(lignes):
#    if i == 0:
#      fic.write(ligne)
#    else:
#      lig = map(float, ligne.rstrip('\n\r').split())
#
#      longueur = len(lig)
#      if longueur == 6:
#        point = Point3D(lig[0], lig[1], lig[2])
#        if intensiteMin <= lig[4] <= intensiteMax and deviationMin <= lig[4] <= deviationMax and zMin <= point.z <= zMax:
#          fic.write(ligne)
#      else:
#        print "Format du fichier ", cheminFichier, " non  pris en charge... "
#        return 0

def ecrireFichierPoints(cheminLas, X, Y, Z, **kwargs):
  fic = open(chemin, "w")
  print "ecriture fichier ", chemin
  fic.write("1 0 0 0 0 1 0 0 0 0 1 0 0 0 0 1\n")
  if not "intensity" in kwargs:
    I = np.ones(len(X))
  else: I = kwargs["intensity"]
  if not "deviation" in kwargs:
    J = np.zeros(len(X))
  else: J = kwargs["deviation"]
  if not "ordre" in kwargs:
    K = np.zeros(len(X))
  else: K = kwargs["ordre"]

  for i in range(len(X)):
    fic.write("%f %f %f %f %f %f\n" % (X[i], Y[i], Z[i], K[i], I[i], J[i]))


  fic.close()

#from Watershed import *
#def appliqueWatershed(cheminImage):
# 
#  shed = Watershed(
#             data_image = cheminImage,
#             binary_or_gray_or_color = "color",
#             size_for_calculations = 128,
#             sigma = 1,
#             gradient_threshold_as_fraction = 0.1,
#             level_decimation_factor = 16,
#             debug = 0,
#         )
#  shed.extract_data_pixels()
#  shed.display_data_image()
#  shed.mark_image_regions_for_gradient_mods()                     #(A)
#  shed.compute_gradient_image()
#  shed.modify_gradients_with_marker_minima()                      #(B)
#  shed.compute_Z_level_sets_for_gradient_image()
#  shed.propagate_influence_zones_from_bottom_to_top_of_Z_levels()
#  shed.display_watershed()
#  shed.display_watershed_in_color()
#  shed.extract_watershed_contours()
#  shed.display_watershed_contours_in_color()

cheminImage = ""
#appliqueWatershed(cheminImage)


def fitGaussienne(onde, afficher = False):
  data = np.asarray(onde)
  X = np.arange(data.size)
  x = np.sum(X * data) / sum(data)
  width = np.sqrt(np.abs(np.sum((X - x) ** 2 * data) / np.sum(data)))
  max = np.max(data)
  fit = lambda t : max * np.exp(-(t - x) ** 2 / (2 * width ** 2))

  solution = fit(X)
  idx = np.where(solution == np.max(solution))[0][0]
  if afficher:
    plt.plot(onde, label = "data")
    plt.plot(solution, label = "fit")
    plt.legend()
    plt.show()
  return width, max

def exempleAfficheTerre():
  from mayavi.sources.builtin_surface import BuiltinSurface

  from mayavi import mlab
  continents_src = BuiltinSurface(source = 'earth', name = 'Continents')
  # The on_ratio of the Earth source controls the level of detail of the
  # continents outline.
  continents_src.data_source.on_ratio = 2
  continents = mlab.pipeline.surface(continents_src, color = (0, 0, 0))

  ###############################################################################
  # Display a semi-transparent sphere, for the surface of the Earth

  # We use a sphere Glyph, throught the points3d mlab function, rather than
  # building the mesh ourselves, because it gives a better transparent
  # rendering.
  sphere = mlab.points3d(0, 0, 0, scale_mode = 'none',
                                  scale_factor = 2,
                                  color = (0.67, 0.77, 0.93),
                                  resolution = 50,
                                  opacity = 0.7,
                                  name = 'Earth')

  # These parameters, as well as the color, where tweaked through the GUI,
  # with the record mode to produce lines of code usable in a script.
  sphere.actor.property.specular = 0.45
  sphere.actor.property.specular_power = 5
  # Backface culling is necessary for more a beautiful transparent
  # rendering.
  sphere.actor.property.backface_culling = True

  ###############################################################################
  # Plot the equator and the tropiques
  theta = np.linspace(0, 2 * np.pi, 100)
  for angle in (-np.pi / 6, 0, np.pi / 6):
      x = np.cos(theta) * np.cos(angle)
      y = np.sin(theta) * np.cos(angle)
      z = np.ones_like(theta) * np.sin(angle)

      mlab.plot3d(x, y, z, color = (1, 1, 1),
                          opacity = 0.2, tube_radius = None)
  mlab.show()




def DfL(Td):
  """ retourne la valeur en metres d'un temps en ns. Utilisé pour convertir les waveform LIDAR"""
  return - Td / 0.299792458

class Point2D:
  def __init__(self, _x = 0, _y = 0):
    self.x = _x
    self.y = _y

  def distance(self, point2D):
    return np.sqrt((point2D.x - self.x) ** 2 + (point2D.y - self.y) ** 2)

  def __str__(self):
    return "(" + str(self.x) + " " + str(self.y) + ")"


class MatriceTransformation():
  def __init__(self, positionDepart = Point3D(0, 0, 0)):

    self.mat = Matrix(4, 4)
    self.mat[0][0] = 1
    self.mat[1][1] = 1
    self.mat[2][2] = 1
    self.mat[3][3] = 1
    self.mat[0][3] = positionDepart.x
    self.mat[1][3] = positionDepart.y
    self.mat[2][3] = positionDepart.z
    self.matriceUnite = self.isIdentite()

  def rotation(self, vecteur):
    return Point3D(vecteur[0] * self.mat[0][0] + vecteur[1] * self.mat[0][1] + vecteur[2] * self.mat[0][2],
            vecteur[0] * self.mat[1][0] + vecteur[1] * self.mat[1][1] + vecteur[2] * self.mat[1][2],
            vecteur[0] * self.mat[2][0] + vecteur[1] * self.mat[2][1] + vecteur[2] * self.mat[2][2])

  def translation(self, vecteur):
    return Point3D(vecteur[0] + self.mat[0][3], vecteur[1] + self.mat[1][3], vecteur[2] + self.mat[2][3])

  def transformation(self, vecteur):
    return Point3D(vecteur[0] * self.mat[0][0] + vecteur[1] * self.mat[0][1] + vecteur[2] * self.mat[0][2] + self.mat[0][3],
            vecteur[0] * self.mat[1][0] + vecteur[1] * self.mat[1][1] + vecteur[2] * self.mat[1][2] + self.mat[1][3],
            vecteur[0] * self.mat[2][0] + vecteur[1] * self.mat[2][1] + vecteur[2] * self.mat[2][2] + self.mat[2][3])

  def transforme(self, vecteur):

    point = Point3D(vecteur[0] * self.mat[0][0] + vecteur[1] * self.mat[0][1] + vecteur[2] * self.mat[0][2] + self.mat[0][3],
            vecteur[0] * self.mat[1][0] + vecteur[1] * self.mat[1][1] + vecteur[2] * self.mat[1][2] + self.mat[1][3],
            vecteur[0] * self.mat[2][0] + vecteur[1] * self.mat[2][1] + vecteur[2] * self.mat[2][2] + self.mat[2][3])
    for i in range(3):
      vecteur[i] = point[i]

  def setMatrice(self, matCopie):
    for i in range(4):
      for j in range(4):
        self.mat[i][j] = matCopie[i * 4 + j]
    self.matriceUnite = self.isIdentite()


  def getMatriceString(self):
    matriceString = ""
    for i in range(4):
      for j in range(4):
        matriceString += str(self.mat[i][j]) + " "
      matriceString += "\n"
    return matriceString
  
  def __repr__(self):
    return self.getMatriceString1Ligne()

  def getMatriceString1Ligne(self):
    matriceString = ""
    for i in range(4):
      for j in range(4):
        matriceString += str(self.mat[i][j]) + " "
    return matriceString

  def isIdentite(self):
    if self.mat[0][0] == 1 and self.mat[1][1] == 1 and self.mat[2][2] == 1 and self.mat[3][3] == 1:
      if self.mat.somme() == 4:
        return True
    else:
      return False

  def isMatriceUnite(self):
    return self.matriceUnite

class MatriceRotationRoulisTangageLacet(MatriceTransformation):
  def __init__(self, roulis, tangage, lacet, positionDepart = Point3D(0, 0, 0)):
    MatriceTransformation.__init__(self, positionDepart)
    self.mat[0][0] = np.cos(tangage) * np.cos(lacet)
    self.mat[0][1] = np.cos(tangage) * np.sin(lacet)
    self.mat[0][2] = -np.sin(tangage)
    self.mat[1][0] = np.sin(roulis) * np.sin(tangage) * np.cos(lacet) - np.cos(roulis) * np.sin(lacet)
    self.mat[1][1] = np.sin(roulis) * np.sin(tangage) * np.sin(lacet) + np.cos(roulis) * np.cos(lacet)
    self.mat[1][2] = np.sin(roulis) * np.cos(tangage)
    self.mat[2][0] = np.cos(roulis) * np.sin(tangage) * np.cos(lacet) + np.sin(roulis) * np.sin(lacet)
    self.mat[2][1] = np.cos(roulis) * np.sin(tangage) * np.sin(lacet) - np.sin(roulis) * np.cos(lacet)
    self.mat[2][2] = np.cos(roulis) * np.cos(tangage)
    

class MatriceRotationAxeX(MatriceTransformation):
# matrice rotation suivant axe X : http://coucousflingueurs.free.fr/docs/Maths%20appliqu%E9es/FAQ%20matrice.htm
#         |  1  0       0       0 |
#     M = |  0  cos(A) -sin(A)  0 |
#         |  0  sin(A)  cos(A)  0 |
#         |  0  0       0       1 |
  def __init__(self, positionDepart = Point3D(0, 0, 0)):
    MatriceTransformation.__init__(self, positionDepart)
    theta = np.pi / 2
    self.mat[0][0] = 1
    self.mat[1][1] = np.cos(theta)
    self.mat[1][2] = np.sin(theta)
    self.mat[2][1] = -np.sin(theta)
    self.mat[2][2] = np.cos(theta)
    self.mat[3][3] = 1


class MatriceRotationAxeY(MatriceTransformation):
# matrice rotation suivant axe X : http://coucousflingueurs.free.fr/docs/Maths%20appliqu%E9es/FAQ%20matrice.htm
#         |  cos(A)  0  -sin(A)  0 |
#     M = |  0       1   0       0 |
#         |  sin(A)  0   cos(A)  0 |
#         |  0       0   0       1 |
  def __init__(self):
    theta = np.pi / 2
    self.mat[0][0] = np.cos(theta)
    self.mat[0][2] = -np.sin(theta)
    self.mat[1][1] = 1
    self.mat[2][0] = np.sin(theta)
    self.mat[2][2] = np.cos(theta)
    self.mat[3][3] = 1


class FichierProprietes():
  def __init__(self, cheminProprietes = "", mode = "lireFichier"):
    self.cheminFichier = cheminProprietes
    if cheminProprietes != "" and not os.path.isfile(cheminProprietes):
      print "le fichier proprietes", cheminProprietes, "n'a pas ete trouve"
#       raise Exception("le fichier proprietes " +  cheminProprietes + " n'a pas ete trouve")
    self.proprietes = {}
    self.proprietes['zoneUtilisateurOuZoneScan'] = 1
    if mode == "lireFichier":
      self.lireFichier()

  def keys(self): return self.proprietes.keys()
  def items(self): return self.proprietes.items()
  def values(self): return self.proprietes.values()
  def __getitem__(self, key):
    if key in self.proprietes: return self.proprietes[key]
    else: return ""
  def __delitem__(self, key):
    if key in self.proprietes : del self.proprietes[key]
  def __len__(self):  return len(self.proprietes)
  def __setitem__(self, key, item): self.proprietes[key] = item

  def lireFichier(self):
    if not os.path.isfile(self.cheminFichier):
      return 0
    propFile = file(self.cheminFichier, "rU")
    propDict = dict()
    for propLine in propFile:
      propDef = propLine.strip()
      if len(propDef) == 0: continue
      if propDef[0] in ('!', '#'): continue
      punctuation = [ propDef.find(c) for c in ':= ' ] + [ len(propDef) ]
      found = min([ pos for pos in punctuation if pos != -1 ])
      name = propDef[:found].rstrip()
      value = propDef[found:].lstrip(":= ").rstrip()
      propDict[name] = value
    propFile.close()
    self.proprietes = propDict
    self.proprietes['zoneUtilisateurOuZoneScan'] = 1

#    for key in self.proprietes.keys():
#      print key, self.proprietes[key]
    return 1

  def ecrireFichier(self):
    if self.cheminFichier:
      if not os.path.isfile(self.cheminFichier):
        if not os.path.isdir(os.path.dirname(self.cheminFichier)):
          print os.path.dirname(self.cheminFichier)
          os.makedirs(os.path.dirname(self.cheminFichier))

      fic = open(self.cheminFichier, "w")
      print "\n###Proprietes:"
      for key, value in self.proprietes.items():
        espaces = ' ' * (35 - len(key))
        print "%s %s: %s" % (key, espaces, value)
        fic.write("%s:%s\n" % (key, value))
      fic.close()
      return 0
    return True
  
  def afficher(self):    
      for key, value in self.proprietes.items():
        espaces = ' ' * (35 - len(key))
        print "%s %s: %s" % (key, espaces, value)


class MatrixError(Exception):
    """ An exception class for Matrix """
    pass

class Matrix(object):
    """ A simple Python matrix class with
    basic operations and operator overloading """

    def __init__(self, m, n, init = True):
        if init:
            self.rows = [[0] * n for x in range(m)]
        else:
            self.rows = []
        self.m = m
        self.n = n

    def __getitem__(self, idx):
        return self.rows[idx]

    def __setitem__(self, idx, item):
        self.rows[idx] = item

    def __str__(self):
        s = '\n'.join([' '.join([str(item) for item in row]) for row in self.rows])
        return s + '\n'

    def __repr__(self):
        s = str(self.rows)
        rank = str(self.getRank())
        rep = "Matrix: \"%s\", rank: \"%s\"" % (s, rank)
        return rep

    def somme(self):
      return np.sum(self.rows)

    def reset(self):
        """ Reset the matrix data """
        self.rows = [[] for x in range(self.m)]

    def transpose(self):
        """ Transpose the matrix. Changes the current matrix """

        self.m, self.n = self.n, self.m
        self.rows = [list(item) for item in zip(*self.rows)]

    def getTranspose(self):
        """ Return a transpose of the matrix without
        modifying the matrix itself """

        m, n = self.n, self.m
        mat = Matrix(m, n)
        mat.rows = [list(item) for item in zip(*self.rows)]

        return mat

    def getRank(self):
        return (self.m, self.n)

    def __eq__(self, mat):
        """ Test equality """

        return (mat.rows == self.rows)

    def __add__(self, mat):
        """ Add a matrix to this matrix and
        return the new matrix. Doesn't modify
        the current matrix """

        if self.getRank() != mat.getRank():
            raise MatrixError, "Trying to add matrixes of varying rank!"

        ret = Matrix(self.m, self.n)

        for x in range(self.m):
            row = [sum(item) for item in zip(self.rows[x], mat[x])]
            ret[x] = row

        return ret

    def __sub__(self, mat):
        """ Subtract a matrix from this matrix and
        return the new matrix. Doesn't modify
        the current matrix """

        if self.getRank() != mat.getRank():
            raise MatrixError, "Trying to add matrixes of varying rank!"

        ret = Matrix(self.m, self.n)

        for x in range(self.m):
            row = [item[0] - item[1] for item in zip(self.rows[x], mat[x])]
            ret[x] = row

        return ret

    def __mul__(self, mat):
        """ Multiple a matrix with this matrix and
        return the new matrix. Doesn't modify
        the current matrix """

        matm, matn = mat.getRank()

        if (self.n != matm):
            raise MatrixError, "Matrices cannot be multipled!"

        mat_t = mat.getTranspose()
        mulmat = Matrix(self.m, matn)

        for x in range(self.m):
            for y in range(mat_t.m):
                mulmat[x][y] = sum([item[0] * item[1] for item in zip(self.rows[x], mat_t[y])])

        return mulmat

    def __iadd__(self, mat):
        """ Add a matrix to this matrix.
        This modifies the current matrix """

        # Calls __add__
        tempmat = self + mat
        self.rows = tempmat.rows[:]
        return self

    def __isub__(self, mat):
        """ Add a matrix to this matrix.
        This modifies the current matrix """

        # Calls __sub__
        tempmat = self - mat
        self.rows = tempmat.rows[:]
        return self

    def __imul__(self, mat):
        """ Add a matrix to this matrix.
        This modifies the current matrix """

        # Possibly not a proper operation
        # since this changes the current matrix
        # rank as well...

        # Calls __mul__
        tempmat = self * mat
        self.rows = tempmat.rows[:]
        self.m, self.n = tempmat.getRank()
        return self

    def save(self, filename):
        open(filename, 'w').write(str(self))

    @classmethod
    def _makeMatrix(cls, rows):

        m = len(rows)
        n = len(rows[0])
        # Validity check
        if any([len(row) != n for row in rows[1:]]):
            raise MatrixError, "inconsistent row length"
        mat = Matrix(m, n, init = False)
        mat.rows = rows

        return mat

    @classmethod
    def makeZero(cls, m, n):
        """ Make a zero-matrix of rank (mxn) """

        rows = [[0] * n for x in range(m)]
        return cls.fromList(rows)

    @classmethod
    def makeId(cls, m):
        """ Make identity matrix of rank (mxm) """

        rows = [[0] * m for x in range(m)]
        idx = 0

        for row in rows:
            row[idx] = 1
            idx += 1

        return cls.fromList(rows)

    @classmethod
    def readStdin(cls):
        """ Read a matrix from standard input """

        print 'Enter matrix row by row. Type "q" to quit'
        rows = []
        while True:
            line = sys.stdin.readline().strip()
            if line == 'q': break

            row = [int(x) for x in line.split()]
            rows.append(row)

        return cls._makeMatrix(rows)

    @classmethod
    def readGrid(cls, fname):
        """ Read a matrix from a file """

        rows = []
        for line in open(fname).readlines():
            row = [int(x) for x in line.split()]
            rows.append(row)

        return cls._makeMatrix(rows)

    @classmethod
    def fromList(cls, listoflists):
        """ Create a matrix by directly passing a list
        of lists """

        # E.g: Matrix.fromList([[1 2 3], [4,5,6], [7,8,9]])

        rows = listoflists[:]
        return cls._makeMatrix(rows)

def printDict(di, format = "%-25s %s"):
    for (key, val) in di.items():
        print format % (str(key) + ':', val)

def dumpObj(obj, maxlen = 100, lindent = 30, maxspew = 600):

    """Print a nicely formatted overview of an object.

    The output lines will be wrapped at maxlen, with lindent of space
    for names of attributes.  A maximum of maxspew characters will be
    printed for each attribute value.

    You can hand dumpObj any data type -- a module, class, instance,
    new class.

    Note that in reformatting for compactness the routine trashes any
    formatting in the docstrings it prints.

    Example:
       >>> class Foo(object):
               a = 30
               def bar(self, b):
                   "A silly method"
                   return a*b
       ... ... ... ... 
       >>> foo = Foo()
       >>> dumpObj(foo)
       Instance of class 'Foo' as defined in module __main__ with id 136863308
       Documentation string:   None
       Built-in Methods:       __delattr__, __getattribute__, __hash__, __init__
                               __new__, __reduce__, __repr__, __setattr__,       
                               __str__
       Methods:
         bar                   "A silly method"
       Attributes:
         __dict__              {}
         __weakref__           None
         a                     30
    """

    import types

    # Formatting parameters.
    ltab = 2    # initial tab in front of level 2 text

    # There seem to be a couple of other types; gather templates of them
    MethodWrapperType = type(object().__hash__)

    #
    # Gather all the attributes of the object
    #
    objclass = None
    objdoc = None
    objmodule = '<None defined>'

    methods = []
    builtins = []
    classes = []
    attrs = []
    for slot in dir(obj):
        attr = getattr(obj, slot)
        if   slot == '__class__':
            objclass = attr.__name__
        elif slot == '__doc__':
            objdoc = attr
        elif slot == '__module__':
            objmodule = attr
        elif (isinstance(attr, types.BuiltinMethodType) or
              isinstance(attr, MethodWrapperType)):
            builtins.append(slot)
        elif (isinstance(attr, types.MethodType) or
              isinstance(attr, types.FunctionType)):
            methods.append((slot, attr))
        elif isinstance(attr, types.TypeType):
            classes.append((slot, attr))
        else:
            attrs.append((slot, attr))

    #
    # Organize them
    #
    methods.sort()
    builtins.sort()
    classes.sort()
    attrs.sort()

    #
    # Print a readable summary of those attributes
    #
    normalwidths = [lindent, maxlen - lindent]
    tabbedwidths = [ltab, lindent - ltab, maxlen - lindent - ltab]

    def truncstring(s, maxlen):
        if len(s) > maxlen:
            return s[0:maxlen] + ' ...(%d more chars)...' % (len(s) - maxlen)
        else:
            return s

    # Summary of introspection attributes
    if objclass == '' or objclass == None:
        objclass = type(obj).__name__
    intro = "Instance of class '%s' as defined in module %s with id %d" % \
            (objclass, objmodule, id(obj))
    print '\n'.join(prettyPrint(intro, maxlen))

    # Object's Docstring
    if objdoc is None:
        objdoc = str(objdoc)
    else:
        objdoc = ('"""' + objdoc.strip() + '"""')
        print
        print prettyPrintCols(('Documentation string:',
                                truncstring(objdoc, maxspew)),
                              normalwidths, ' ')

    # Built-in methods
    if builtins:
        bi_str = delchars(str(builtins), "[']") or str(None)
        print
        print prettyPrintCols(('Built-in Methods:',
                                truncstring(bi_str, maxspew)),
                              normalwidths, ', ')

    # Classes
    if classes:
        print
        print 'Classes:'
    for (classname, classtype) in classes:
        classdoc = getattr(classtype, '__doc__', None) or '<No documentation>'
        print prettyPrintCols(('',
                                classname,
                                truncstring(classdoc, maxspew)),
                              tabbedwidths, ' ')

    # User methods
    if methods:
        print
        print 'Methods:'
    for (methodname, method) in methods:
        methoddoc = getattr(method, '__doc__', None) or '<No documentation>'
        print prettyPrintCols(('',
                                methodname,
                                truncstring(methoddoc, maxspew)),
                              tabbedwidths, ' ')

    # Attributes
    if attrs:
        print
        print 'Attributes:'
    for (attr, val) in attrs:
        print prettyPrintCols(('',
                                attr,
                                truncstring(str(val), maxspew)),
                              tabbedwidths, ' ')

def prettyPrintCols(strings, widths, split = ' '):
    """Pretty prints text in colums, with each string breaking at
    split according to prettyPrint.  margins gives the corresponding
    right breaking point."""

    assert len(strings) == len(widths)

    strings = map(nukenewlines, strings)

    # pretty print each column
    cols = [''] * len(strings)
    for i in range(len(strings)):
        cols[i] = prettyPrint(strings[i], widths[i], split)

    # prepare a format line
    format = ''.join(["%%-%ds" % width for width in widths[0:-1]]) + "%s"

    def formatline(*cols):
        return format % tuple(map(lambda s: (s or ''), cols))

    # generate the formatted text
    return '\n'.join(map(formatline, *cols))

def prettyPrint(string, maxlen = 100, split = ' '):
    """Pretty prints the given string to break at an occurrence of
    split where necessary to avoid lines longer than maxlen.

    This will overflow the line if no convenient occurrence of split
    is found"""

    # Tack on the splitting character to guarantee a final match
    string += split

    lines = []
    oldeol = 0
    eol = 0
    while not (eol == -1 or eol == len(string) - 1):
        eol = string.rfind(split, oldeol, oldeol + maxlen + len(split))
        lines.append(string[oldeol:eol])
        oldeol = eol + len(split)

    return lines

def nukenewlines(string):
  """Strip newlines and any trailing/following whitespace; rejoin
  with a single space where the newlines were.
  
  Bug: This routine will completely butcher any whitespace-formatted
  text."""

  if not string: return ''
  lines = string.splitlines()
  return ' '.join([line.strip() for line in lines])

def delchars(str, chars):
  """Returns a string for which all occurrences of characters in
  chars have been removed."""

  # Translate demands a mapping string of 256 characters;
  # whip up a string that will leave all characters unmolested.
  identity = ''.join([chr(x) for x in range(256)])

  return str.translate(identity, chars)


def sortedDictValues(adict):
  """retourne le dictionnaire rangé âr ordre croissant"""
  items = adict.items()
  items.sort()
  return [value for key, value in items]


# ------------------------------------------------------------------------
def safe_unicode(obj, *args):
    """ return the unicode representation of obj """
    try:
        return unicode(obj, *args)
    except UnicodeDecodeError:
        # obj is byte string
        ascii_text = str(obj).encode('string_escape')
        return unicode(ascii_text)

def safe_str(obj):
    """ return the byte string representation of obj """
    try:
        return str(obj)
    except UnicodeEncodeError:
        # obj is unicode
        return unicode(obj).encode('unicode_escape')


# Sample code below to illustrate their usage
#def write_unicode_to_file(filename, unicode_text):
#    """
#    Write unicode_text to filename in UTF-8 encoding.
#    Parameter is expected to be unicode. But it will also tolerate byte string.
#    """
#    fp = file(filename,'wb')
#    # workaround problem if caller gives byte string instead
#    unicode_text = safe_unicode(unicode_text)
#    utf8_text = unicode_text.encode('utf-8')
#    fp.write(utf8_text)
#    fp.close()
# ------------------------------------------------------------------------


class EasyAccessDict(dict):
  def __getattr__(self, name):
    if name in self:
        return self[name]
    n = EasyAccessDict()
    self.__setitem__(name, n)
    return n
  def __getitem__(self, name):
    if name not in self:
        self.__setitem__(name, EasyAccessDict())
    return dict.__getitem__(self, name)
  def __setattr__(self, name, value):
    self.__setitem__(name, value)


def getPDFContent(path):
  import pyPdf
  content = ""
  # Load PDF into pyPDF
  pdf = pyPdf.PdfFileReader(file(path, "rb"))
  # Iterate pages
  for i in range(0, pdf.getNumPages()):
      # Extract text from page and add to content
      content += pdf.getPage(i).extractText() + "\n"
  # Collapse whitespace
  content = " ".join(content.replace(u"\xa0", " ").strip().split())
  return content

#print getPDFContent("test.pdf").encode("ascii", "ignore")

def lirefichierExcel():
  print "methode en commentaire : necessite l'installation de openpyxl"
#  fic = '/media/DATA/donnees/reunion/Inventaire_Parcelles_FARCE_pos.xlsx'
#  from openpyxl import load_workbook
#
#  wb = load_workbook(filename = fic)
#
#  print wb.get_sheet_names()
#
#  sheet = wb.get_sheet_by_name('Maido Tamarin')
#  print sheet.calculate_dimension()
#
#  cells = sheet.range("A1:K1", row = 0, column = 0)
#  for ligne in cells:
#    for cell in ligne:
#      print cell.value
#  cells = sheet.range("A2:K178", row = 0, column = 0)
#  for ligne in cells:
#    for cell in ligne:
#      print cell.value
#    print "\n"
#  sheet_ranges = wb['range names']  
#  print sheet_ranges['D18'].value # D18



def savitzky_golay(y, window_size, order, deriv = 0):
    r"""Smooth (and optionally differentiate) data with a Savitzky-Golay filter.
    The Savitzky-Golay filter removes high frequency noise from data.
    It has the advantage of preserving the original shape and
    features of the signal better than other types of filtering
    approaches, such as moving averages techhniques.
    Parameters
    ----------
    y : array_like, shape (N,)
        the values of the time history of the signal.
    window_size : int
        the length of the window. Must be an odd integer number.
    order : int
        the order of the polynomial used in the filtering.
        Must be less then `window_size` - 1.
    deriv: int
        the order of the derivative to compute (default = 0 means only smoothing)
    Returns
    -------
    ys : ndarray, shape (N)
        the smoothed signal (or it's n-th derivative).
    Notes
    -----
    The Savitzky-Golay is a type of low-pass filter, particularly
    suited for smoothing noisy data. The main idea behind this
    approach is to make for each point a least-square fit with a
    polynomial of high order over a odd-sized window centered at
    the point.
    Examples
    --------
    t = np.linspace(-4, 4, 500)
    y = np.exp( -t**2 ) + np.random.normal(0, 0.05, t.shape)
    ysg = savitzky_golay(y, window_size=31, order=4)
    import matplotlib.pyplot as plt
    plt.plot(t, y, label='Noisy signal')
    plt.plot(t, np.exp(-t**2), 'k', lw=1.5, label='Original signal')
    plt.plot(t, ysg, 'r', label='Filtered signal')
    plt.legend()
    plt.show()
    References
    ----------
    .. [1] A. Savitzky, M. J. E. Golay, Smoothing and Differentiation of
       Data by Simplified Least Squares Procedures. Analytical
       Chemistry, 1964, 36 (8), pp 1627-1639.
    .. [2] Numerical Recipes 3rd Edition: The Art of Scientific Computing
       W.H. Press, S.A. Teukolsky, W.T. Vetterling, B.P. Flannery
       Cambridge University Press ISBN-13: 9780521880688
    """
    try:
        window_size = np.abs(np.int(window_size))
        order = np.abs(np.int(order))
    except ValueError, msg:
        raise ValueError("window_size and order have to be of type int")
    if window_size % 2 != 1 or window_size < 1:
        raise TypeError("window_size size must be a positive odd number")
    if window_size < order + 2:
        raise TypeError("window_size is too small for the polynomials order")
    order_range = range(order + 1)
    half_window = (window_size - 1) // 2
    # precompute coefficients
    b = np.mat([[k ** i for i in order_range] for k in range(-half_window, half_window + 1)])
    m = np.linalg.pinv(b).A[deriv]
    # pad the signal at the extremes with
    # values taken from the signal itself
    firstvals = y[0] - np.abs(y[1:half_window + 1][::-1] - y[0])
    lastvals = y[-1] + np.abs(y[-half_window - 1:-1][::-1] - y[-1])
    y = np.concatenate((firstvals, y, lastvals))
    return np.convolve(m, y, mode = 'valid')


def savitzky_golay_piecewise(xvals, data, kernel = 11, order = 4):
    turnpoint = 0
    last = len(xvals)
    if xvals[1] > xvals[0] : #x is increasing?
        for i in range(1, last) : #yes
            if xvals[i] < xvals[i - 1] : #search where x starts to fall
                turnpoint = i
                break
    else: #no, x is decreasing
        for i in range(1, last) : #search where it starts to rise
            if xvals[i] > xvals[i - 1] :
                turnpoint = i
                break
    if turnpoint == 0 : #no change in direction of x
        return savitzky_golay(data, kernel, order)
    else:
        #smooth the first piece
        firstpart = savitzky_golay(data[0:turnpoint], kernel, order)
        #recursively smooth the rest
        rest = savitzky_golay_piecewise(xvals[turnpoint:], data[turnpoint:], kernel, order)
        return numpy.concatenate((firstpart, rest))


def sgolay2d (z, window_size, order, derivative = None):
    """
    Smoothing de courbe 2D
    """
    # number of terms in the polynomial expression
    n_terms = (order + 1) * (order + 2) / 2.0

    if  window_size % 2 == 0:
        raise ValueError('window_size must be odd')

    if window_size ** 2 < n_terms:
        raise ValueError('order is too high for the window size')

    half_size = window_size // 2

    # exponents of the polynomial.
    # p(x,y) = a0 + a1*x + a2*y + a3*x^2 + a4*y^2 + a5*x*y + ...
    # this line gives a list of two item tuple. Each tuple contains
    # the exponents of the k-th term. First element of tuple is for x
    # second element for y.
    # Ex. exps = [(0,0), (1,0), (0,1), (2,0), (1,1), (0,2), ...]
    exps = [ (k - n, n) for k in range(order + 1) for n in range(k + 1) ]

    # coordinates of points
    ind = np.arange(-half_size, half_size + 1, dtype = np.float64)
    dx = np.repeat(ind, window_size)
    dy = np.tile(ind, [window_size, 1]).reshape(window_size ** 2,)

    # build matrix of system of equation
    A = np.empty((window_size ** 2, len(exps)))
    for i, exp in enumerate(exps):
        A[:, i] = (dx ** exp[0]) * (dy ** exp[1])

    # pad input array with appropriate values at the four borders
    new_shape = z.shape[0] + 2 * half_size, z.shape[1] + 2 * half_size
    Z = np.zeros((new_shape))
    # top band
    band = z[0, :]
    Z[:half_size, half_size:-half_size] = band - np.abs(np.flipud(z[1:half_size + 1, :]) - band)
    # bottom band
    band = z[-1, :]
    Z[-half_size:, half_size:-half_size] = band + np.abs(np.flipud(z[-half_size - 1:-1, :]) - band)
    # left band
    band = np.tile(z[:, 0].reshape(-1, 1), [1, half_size])
    Z[half_size:-half_size, :half_size] = band - np.abs(np.fliplr(z[:, 1:half_size + 1]) - band)
    # right band
    band = np.tile(z[:, -1].reshape(-1, 1), [1, half_size])
    Z[half_size:-half_size, -half_size:] = band + np.abs(np.fliplr(z[:, -half_size - 1:-1]) - band)
    # central band
    Z[half_size:-half_size, half_size:-half_size] = z

    # top left corner
    band = z[0, 0]
    Z[:half_size, :half_size] = band - np.abs(np.flipud(np.fliplr(z[1:half_size + 1, 1:half_size + 1])) - band)
    # bottom right corner
    band = z[-1, -1]
    Z[-half_size:, -half_size:] = band + np.abs(np.flipud(np.fliplr(z[-half_size - 1:-1, -half_size - 1:-1])) - band)

    # top right corner
    band = Z[half_size, -half_size:]
    Z[:half_size, -half_size:] = band - np.abs(np.flipud(Z[half_size + 1:2 * half_size + 1, -half_size:]) - band)
    # bottom left corner
    band = Z[-half_size:, half_size].reshape(-1, 1)
    Z[-half_size:, :half_size] = band - np.abs(np.fliplr(Z[-half_size:, half_size + 1:2 * half_size + 1]) - band)

    # solve system and convolve
    if derivative == None:
        m = np.linalg.pinv(A)[0].reshape((window_size, -1))
        return scipy.signal.fftconvolve(Z, m, mode = 'valid')
    elif derivative == 'col':
        c = np.linalg.pinv(A)[1].reshape((window_size, -1))
        return scipy.signal.fftconvolve(Z, -c, mode = 'valid')
    elif derivative == 'row':
        r = np.linalg.pinv(A)[2].reshape((window_size, -1))
        return scipy.signal.fftconvolve(Z, -r, mode = 'valid')
    elif derivative == 'both':
        c = np.linalg.pinv(A)[1].reshape((window_size, -1))
        r = np.linalg.pinv(A)[2].reshape((window_size, -1))
        return scipy.signal.fftconvolve(Z, -r, mode = 'valid'), scipy.signal.fftconvolve(Z, -c, mode = 'valid')
