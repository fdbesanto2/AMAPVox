# -*- coding: UTF-8 -*-
'''
Created on 3 avr. 2014

@author: grau
'''
from PyQt4.QtGui import *
from PyQt4.QtCore import *
from decimal import *
import ihmVoxels
import ihmToolbox
from libVoxels import *

#commande pour compiler le fichier ui:
#cd ~/workspace/PY/CodesEloi/src/ihm/
#pyuic4 voxelisation2.ui -o ihmVoxels.py
#pyuic4 toolbox.ui -o ihmToolbox.py

chemin = os.getcwd()
#chemin = "/media/DATA/grau_lidar/LidarTerrestre/outputPlacettes/pla19"

if sys.platform == "win32":
  plateforme = "win"
  slash = "\\"
  import ctypes
  myappid = 'voxelisation' # arbitrary string
  ctypes.windll.shell32.SetCurrentProcessExplicitAppUserModelID(myappid)
else:
  plateforme = "linux"
  slash = "/"

cheminExecutableParDefaut = chemin + slash + "bin" + slash + "voxelisation.exe"
cheminAideParDefaut = chemin + slash + "bin" + slash + "documentationVoxels.pdf"
dossierParDefaut = chemin + slash + "user_data" + slash
logParDefaut = ""

class IHMVoxelsMain(QMainWindow, ihmVoxels.Ui_MainWindow):
  def __init__(self, parent = None):
    super(IHMVoxelsMain, self).__init__(parent)

    self.setupUi(self)
    size_ecran = QDesktopWidget().screenGeometry()
    size_fenetre = self.geometry()
    self.move((size_ecran.width() - size_fenetre.width()) / 2, (size_ecran.height() - size_fenetre.height()) / 2)

    self.properties = FichierProprietes()
    self.toolbox = []

    self.initConfig()
    self.initFrameLastFile()
    self.initToolBox()

    self.initActions()
    self.initFileToolbar()

    self.show()

  def initConfig(self):
    self.cheminAide = cheminAideParDefaut
    self.text_cheminExec.setText(cheminExecutableParDefaut)
    self.text_cheminLog.setText(logParDefaut)
    self.text_repertoireDefaut.setText(dossierParDefaut)

  def initToolBox(self):
    self.toolbox.append(toolBox(self.getRepertoirePoints, self.getRepertoireDensite, self.text_dossierOutput, self.ecritConsole, self.tabtoolBox))


  def initActions(self):
    self.connect(self.combo_box_Mode, SIGNAL('currentIndexChanged(const QString&)'), self.OnModeChanged)
    self.connect(self.combo_box_listeScan, SIGNAL('currentIndexChanged(const QString&)'), self.OnListeScanChanged)
    self.connect(self.combo_box_listeTypeExtraction, SIGNAL('currentIndexChanged(const QString&)'), self.OnModeChanged)
    self.connect(self.combo_box_listeTypeMultiEcho, SIGNAL('currentIndexChanged(const QString&)'), self.OnModeChanged)
    self.connect(self.combo_box_ponderation, SIGNAL('currentIndexChanged(const QString&)'), self.OnTypePonderationChanged)

    self.connect(self.combo_box_listeTypeMultiEcho, SIGNAL('currentIndexChanged(const QString&)'), self.OnModeChanged)
    self.connect(self.boutonDossierOutput, SIGNAL("released()"), self.OnSearch_dossierOutput);
    self.connect(self.boutonInputRxp, SIGNAL("released()"), self.OnSearch_fichierRXP);
    self.connect(self.boutonInputTxt, SIGNAL("released()"), self.OnSearch_fichierXYZ);
    self.connect(self.boutonProjetRiegl, SIGNAL("released()"), self.OnSearch_projRiegl);
    self.connect(self.boutonMatriceSOPSuppl, SIGNAL("released()"), self.OnSearch_matSOPSupplementaire);
    self.connect(self.boutonverifier, SIGNAL("released()"), self.OnResolutionChanged);
    self.connect(self.boutonExecuter, SIGNAL("released()"), self.OnExecuter);

    self.connect(self.cbox_iFiltre, SIGNAL('stateChanged(int)'), self.oniFiltreChanged);
    self.connect(self.cbox_dFiltre, SIGNAL('stateChanged(int)'), self.ondFiltreChanged);
    self.connect(self.cbox_thetaFiltre, SIGNAL('stateChanged(int)'), self.onThetaFiltreChanged);
    self.connect(self.cbox_matSOP, SIGNAL('stateChanged(int)'), self.onMatriceSOPSupplementaireChanged);

    self.connect(self.boutonLog, SIGNAL("released()"), self.OnSearch_fichierLog);
    self.connect(self.boutonCheminExec, SIGNAL("released()"), self.OnSearch_cheminExecutable);
    self.connect(self.boutonRepertoireDefaut, SIGNAL("released()"), self.OnSearch_repertoireDefaut);
    self.connect(self.boutonFichierPonderation, SIGNAL("released()"), self.OnSearch_fichierPonderation);



    self.connect(self.actionNouveau_fichier, SIGNAL("triggered()"), self.OnNew)
    self.connect(self.actionOuvrir_properties, SIGNAL("triggered()"), self.OnOpen)
    self.connect(self.action_propos, SIGNAL("triggered()"), self.OnAPropos)
    self.connect(self.actionEnregistrer_utilisateur, SIGNAL("triggered()"), self.OnEnregistrementUtilisateur)
    self.connect(self.actionSauvegarder, SIGNAL("triggered()"), self.sauvegarderFichier)
    self.connect(self.actionSauvegarder_sous, SIGNAL("triggered()"), self.OnSaveAs)
    self.connect(self.actionDocumentation, SIGNAL("triggered()"), self.afficheAide)
    self.connect(self.actionExplorer, SIGNAL("triggered()"), self.ouvrirExplorateur)

  def initFileToolbar(self):
    new = QAction(QIcon.fromTheme("document-new"), 'New', self)
    new.setShortcut('Ctrl+N')
    new.connect(new, SIGNAL('triggered()'), self.OnNew)

    opener = QAction(QIcon.fromTheme('document-open'), 'Open', self)
    opener.setShortcut('Ctrl+O')
    opener.connect(opener, SIGNAL('triggered()'), self.OnOpen) #SLOT('close()'))

    save = QAction(QIcon.fromTheme('document-save'), 'Save', self)
    save.setShortcut('Ctrl+S')
    save.connect(save, SIGNAL('triggered()'), self.sauvegarderFichier)

    saveas = QAction(QIcon.fromTheme('document-save-as'), 'Save As', self)
    saveas.setShortcut('Ctrl+W')
    saveas.connect(saveas, SIGNAL('triggered()'), self.OnSaveAs)

    exit = QAction(QIcon.fromTheme('process'), 'Exit', self)
    exit.setShortcut('Ctrl+Q')
    self.connect(exit, SIGNAL('triggered()'), SLOT('close()'))


    help = QAction(QIcon.fromTheme('help'), 'Aide', self)
    help.setShortcut('Ctrl+H')
    self.connect(help, SIGNAL('triggered()'), self.afficheAide)

    executer = QAction(QIcon.fromTheme('application-x-executable'), 'Executer la voxelisation', self)
    executer.setShortcut('Ctrl+A')
    self.connect(executer, SIGNAL('triggered()'), self.OnExecuter)

#
    calculMNT = QAction(QIcon.fromTheme('image-x-generic'), 'MNT (maille)', self)
    calculMNT.setShortcut('Ctrl+M')
    self.connect(calculMNT, SIGNAL('triggered()'), self.toolbox[0].calculerMNT)

#    afficheToolbox = QAction(QIcon.fromTheme('image-x-generic'), 'extraire points sur couche', self)
#    self.connect(afficheToolbox, SIGNAL('triggered()'), self.afficherToolbox)


    visuFichierPointsLAS = QAction(QIcon.fromTheme('image-x-generic'), 'visualiser un fichier de points LAS', self)
    self.connect(visuFichierPointsLAS, SIGNAL('triggered()'), self.toolbox[0].visualiserFichierPointsLAS)

    visuFichierPointsALS = QAction(QIcon.fromTheme('image-x-generic'), 'visualiser un fichier de points ALS', self)
    self.connect(visuFichierPointsALS, SIGNAL('triggered()'), self.toolbox[0].visualiserFichierPointsALS)

    visuHistogrammeDensite = QAction(QIcon.fromTheme('image-x-generic'), u'visualiser la distribution de densité', self)
    self.connect(visuHistogrammeDensite, SIGNAL('triggered()'), self.toolbox[0].visualiserDistributionDensite)

    visuProfilLAI = QAction(QIcon.fromTheme('image-x-generic'), u'visualiser le profil de LAI', self)
    self.connect(visuProfilLAI, SIGNAL('triggered()'), self.toolbox[0].visualiserProfilLAI)

    visuMailleFiltree = QAction(QIcon.fromTheme('image-x-generic'), u'visualiser la maille en 3 classes de densité', self)
    self.connect(visuMailleFiltree, SIGNAL('triggered()'), self.toolbox[0].visualiserMailleFiltree)

    visuMailleFiltree = QAction(QIcon.fromTheme('image-x-generic'), u'visualiser la maille en 3 classes de densité', self)
    self.connect(visuMailleFiltree, SIGNAL('triggered()'), self.toolbox[0].visualiserMailleFiltree)


    newToolBox = QAction(QIcon.fromTheme('process'), u'Nouvelle Toolbox', self)
    self.connect(newToolBox, SIGNAL('triggered()'), self.lancerNouvelleToolbox)

    self.fileToolbar = self.addToolBar('File')
    self.fileToolbar.addAction(new)
    self.fileToolbar.addAction(opener)
    self.fileToolbar.addAction(save)
    self.fileToolbar.addAction(saveas)
    self.fileToolbar.addSeparator();
    self.fileToolbar.addAction(newToolBox)
    self.fileToolbar = self.addToolBar('Densite')
    self.fileToolbar.addAction(visuFichierPointsALS)
    self.fileToolbar.addAction(visuFichierPointsLAS)
    self.fileToolbar.addSeparator();
    self.fileToolbar.addAction(visuHistogrammeDensite)
    self.fileToolbar.addAction(visuMailleFiltree)
    self.fileToolbar.addAction(visuProfilLAI)
    self.fileToolbar.addSeparator();
    self.fileToolbar.addAction(calculMNT)
#    self.fileToolbar.addSeparator();
#    self.fileToolbar.addAction(afficheToolbox)
    self.fileToolbar = self.addToolBar('Execution')
    self.fileToolbar.addAction(executer)
    self.fileToolbar.addSeparator();
    self.fileToolbar.addAction(exit)

  def lancerNouvelleToolbox(self):
    self.toolbox.append(toolBox(self.getRepertoirePoints, self.getRepertoireDensite, self.text_dossierOutput, self.ecritConsole))

  def ouvrirExplorateur(self):
    if plateforme == "win":
      os.system('open '+ self.getRepertoireCourant())
    else:
      os.system("xdg-open " + self.getRepertoireCourant())
    
  def afficheAide(self):
    if plateforme == "win":
      os.startfile(self.cheminAide)
    else:
      self.previewReport(self.cheminAide)

  def OnExecuter(self):
    if not os.path.isdir(self.text_dossierOutput.text()):
     self.ecritConsole(u"dossier output inexistant. Le programme de voxelisation va tenter de la créer")

    if not os.path.isfile(unicode(self.text_cheminExec.text())):
      self.ecritConsole("executable manquant :" + unicode(self.text_cheminExec.text()))
      return

    fichierSauvegardee = self.sauvegarderFichier()
    if fichierSauvegardee:
      if self.text_cheminLog.text() != "":
        commande = unicode(self.text_cheminExec.text()) + " -i " + unicode(self.properties.cheminFichier) + " 1>" + unicode(self.text_cheminLog.text()) + " 2>" + unicode(self.text_cheminLog.text())
      else:
        commande = unicode(self.text_cheminExec.text()) + " -i " + unicode(self.properties.cheminFichier)


      self.textConsole.append("COMMANDE : " + commande)
      proc = subprocess.Popen(commande, shell = True)
      self.toolbox[0].maille = Maille()
#    proc = subprocess.Popen(commande, shell = True, stdout = subprocess.PIPE, stderr = subprocess.PIPE)
#    out, err = proc.communicate()
#    self.textConsole.append(QString(out))
#    self.textConsole.append(QString(err))


  def creerFrameLastFile(self):
    cheminCourant = os.getcwd()
#    try:
    fic = open(cheminCourant + '/bin/.paramIHMVoxels', "w")
    fic.write(self.properties.cheminFichier + "\n")
    fic.write(unicode(self.text_cheminExec.text()) + "\n")
    fic.write(unicode(self.text_cheminLog.text()) + "\n")
    fic.write(unicode(self.text_repertoireDefaut.text()) + "\n")
#    fic.write(unicode(self.text_iMin_visu.text()) + " " + unicode(self.text_iMax_visu.text()) + " " + unicode(self.text_dMin_visu.text()) + " " + unicode(self.text_dMax_visu.text()) + " " + unicode(self.text_zMin_visu.text()) + " " + unicode(self.text_zMax_visu.text()))
    fic.close()
#    except :
#      print "Impossible de sauvegarder le fichier de configuration de l'IHM ("+cheminCourant + '/bin/.paramIHMVoxels' + "); vérifier les permissions en ecriture du dossier courant ?"


  def initFrameLastFile(self):
    cheminCourant = os.getcwd()
#    print cheminCourant, os.path.isfile(cheminCourant + '/bin/.paramIHMVoxels')
    if os.path.isfile(cheminCourant + '/bin/.paramIHMVoxels'):
      fic = open(cheminCourant + '/bin/.paramIHMVoxels', "r")
      dernierFichier = fic.readline().strip('\n\r')
#      print "dernierFichier", dernierFichier
        
      cheminExec = fic.readline().strip('\n\r')
#      print "cheminExec", cheminExec
      if os.path.isfile(cheminExec):
        self.text_cheminExec.setText(cheminExec)
      log = fic.readline().strip('\n\r')
#      print "log", log
      if os.path.isfile(log):
        self.text_cheminLog.setText(log)

      dossierdefaut = fic.readline().strip('\n\r')

      if os.path.isdir(dossierdefaut):
        self.text_repertoireDefaut.setText(dossierdefaut)

      fic.close()
      
      if os.path.isfile(dernierFichier):
        msgBox = QMessageBox(self)
        msgBox.setWindowTitle(u"Ouvrir... " + dernierFichier)
        msgBox.setText(u"Dernier fichier ouvert : " + dernierFichier)
        msgBox.setIcon(QMessageBox.Information)
        ouvrir = msgBox.addButton('Ouvrir ce fichier', QMessageBox.ActionRole)
        ouvrir.setDefault(True)
        nouveau = msgBox.addButton(u'Créer un nouveau fichier', QMessageBox.ActionRole)
        autre = msgBox.addButton('Ouvrir un autre fichier', QMessageBox.ActionRole)
        sortie = msgBox.addButton('Annuler', QMessageBox.RejectRole)
        ret = msgBox.exec_()
        if msgBox.clickedButton() == ouvrir:
          self.ouvrirFichier(dernierFichier)
        elif msgBox.clickedButton() == nouveau:
          self.OnNew()
        elif msgBox.clickedButton() == autre:
          self.OnOpen()
        else:
          exit()
      else:
        msgBox = QMessageBox(self)
        msgBox.setWindowTitle(u"Ouvrir... ")
        msgBox.setText(u"Voulez vous ? ")
        msgBox.setIcon(QMessageBox.Information)
        ouvrir = msgBox.addButton(u'Ouvrir un fichier existant', QMessageBox.ActionRole)
        ouvrir.setDefault(True)
        nouveau = msgBox.addButton(u'Créer un nouveau fichier', QMessageBox.ActionRole)
        sortie = msgBox.addButton('Annuler', QMessageBox.RejectRole)
        ret = msgBox.exec_()
        if msgBox.clickedButton() == ouvrir:
          self.OnOpen()
        elif msgBox.clickedButton() == nouveau:
          self.OnNew()
        else:
          exit()

    else:
      self.text_cheminExec.setText(cheminExecutableParDefaut)
      self.text_repertoireDefaut.setText(dossierParDefaut)
              
      msgBox = QMessageBox(self)
      msgBox.setWindowTitle(u"Bienvenue")
      msgBox.setText(u"Première utilisation :")
      msgBox.setIcon(QMessageBox.Information)
      msgBox.setInformativeText(u"<center> <center><a href=\"http://tinyurl.com/FormulaireVoxelisation\">Si vous n'etes pas encore enregistré, rendez-vous sur http://tinyurl.com/FormulaireVoxelisation...</a><br/><br/> Veuillez vérifier la configuration de l\'IHM (chemins par défaut et executable dans l'onglet configuration). <br/> Sachez qu\'a chaque execution du programme de voxelisation, les paramètres sont enregistrés dans un fichier : veuillez choisir le chemin vers votre premier fichier.</center>")
      nouveau = msgBox.addButton(u'Nouveau fichier de paramètres' , QMessageBox.AcceptRole)
      nouveau.setDefault(True)
      ouvrir = msgBox.addButton('Ouvrir un fichier existant', QMessageBox.RejectRole)
      ret = msgBox.exec_();
      if msgBox.clickedButton() == nouveau:
        self.OnNew()
      elif msgBox.clickedButton() == ouvrir:
        self.OnOpen()
    
    self.OnModeChanged()

  def OnNew(self):
    dir = self.getRepertoireCourant()
    chemin = QFileDialog.getSaveFileName(self, u"Choisissez le nom et l'emplacement du nouveau fichier de paramètres", dir)
    self.properties.cheminFichier = str(chemin)
    self.ecritConsole("fichier courant : " + self.properties.cheminFichier)
    self.updateWindowTitle()

  def OnOpen(self):
    dir = self.getRepertoireCourant()
    chemin = unicode(QFileDialog.getOpenFileName(self, u"Ouvrir un fichier de paramètres", dir))
    print chemin
    self.ouvrirFichier(chemin)
    if not self.toolbox:
      self.initToolBox()
    self.toolbox[0].setTextDossierOutput(self.text_dossierOutput)
    self.updateWindowTitle()
    
  def updateWindowTitle(self):
    self.setWindowTitle(u"Voxelisation : " + unicode(self.properties.cheminFichier))

  def OnEnregistrementUtilisateur(self):
    QMessageBox.information(self, u"Enregistrement", u"<center><a href=\"http://tinyurl.com/FormulaireVoxelisation\">Enregistrement en ligne</a></center>")
    
  def OnAPropos(self):
    QMessageBox.about(self, u"A propos...", u"<center>Programme de voxelisation de données Lidar Terrestre.<br /> Eloi Grau (eloi.grau@gmail.com)<br />  2014/center>")

  def sauvegarderFichier(self):
    if self.properties.cheminFichier == "":
      self.OnSaveAs()

    if os.path.isfile(self.properties.cheminFichier):
      ecraser_msg = "<center>Ecraser fichier proprietes : " + self.properties.cheminFichier + "?</center>"
      reply = QMessageBox.question(self, 'Message',
                     ecraser_msg, QMessageBox.Yes, QMessageBox.No)
    else:reply = QMessageBox.Yes

    if reply == QMessageBox.Yes:
      self.updateFichierProprietesDepuisIHM()
      if self.properties.ecrireFichier() == 0:
        self.ecritConsole(u"fichier sauvegardé : " + self.properties.cheminFichier)
        self.updateWindowTitle()
        print u"fichier sauvegardé : " + self.properties.cheminFichier
        return True
      else:
        self.ecritConsole(u"le fichier : " + self.properties.cheminFichier + u" n''a pas pu etre sauvegardé")
    return False


  def ouvrirFichier(self, fichier):
    self.properties.cheminFichier = unicode(fichier)
    self.properties.lireFichier()
    self.ecritConsole("fichier courant : " + self.properties.cheminFichier)
    self.updateIHMDepuisProprietes()
    self.OnModeChanged()
    self.OnListeScanChanged()

  def OnSaveAs(self):
    dir = self.getRepertoireCourant()
    chemin = QFileDialog.getSaveFileName(self, "Sauvegarder nouveau fichier Proprietes sous...", dir)
    if chemin:
      self.properties.cheminFichier = unicode(chemin)
      self.sauvegarderFichier()

  def OnTypePonderationChanged(self):
    if self.combo_box_Mode.currentIndex() == 2 or self.combo_box_Mode.currentIndex() == 3 or self.combo_box_Mode.currentIndex() == 5:
      if self.combo_box_ponderation.currentIndex() == 2:
        self.text_FichierPonderation.setVisible(True)
      else:
        self.text_FichierPonderation.setVisible(False)
    else:
      self.text_FichierPonderation.setVisible(False)
      

  def verifierNombreDecimal(self, nombre):
    partie_entiere, partie_decimale = unicode(nombre).split(".")
    nn = Decimal(nombre)
    i_n = int(nn)
    partieDecimal = nn - i_n
    pasPuissance2 = Decimal(1) / Decimal(2 ** (10 + len(partie_decimale)))
    context2 = Context(prec = 10, rounding = ROUND_DOWN)
    quotient = context2.divide_int(partieDecimal , pasPuissance2)
    nombre = i_n + quotient * pasPuissance2
    return nombre

  def OnResolutionChanged(self):
    nombre = float(self.text_Resolution.text())
    try:
      n2 = self.verifierNombreDecimal(nombre)
      self.text_Resolution.setText(unicode(n2))
    except ValueError:
      self.ecritConsole("resolution non valide")

  def closeEvent(self, event):
    self.sauvegarderFichier()
    self.creerFrameLastFile()


  def manageInputTXTRXP(self):
    if self.combo_box_listeScan.currentIndex() == 0:
      self.text_inputFichierXYZ.setVisible(False)
      self.text_inputFichierRXP.setVisible(False)
      self.text_nomScan.setVisible(False)
    if self.combo_box_listeScan.currentIndex() == 1:
      self.text_inputFichierXYZ.setVisible(False)
      self.text_inputFichierRXP.setVisible(False)
      self.text_nomScan.setVisible(True)
    elif self.combo_box_listeScan.currentIndex() == 2:
      self.text_inputFichierXYZ.setVisible(True)
      self.text_inputFichierRXP.setVisible(False)
      self.text_nomScan.setVisible(False)
    elif self.combo_box_listeScan.currentIndex() == 3:
      self.text_inputFichierXYZ.setVisible(False)
      self.text_inputFichierRXP.setVisible(True)
      self.text_nomScan.setVisible(False)

  def blockSignalsComboBox(self, val = True):
    self.combo_box_Mode.blockSignals(val)
    self.combo_box_listeScan.blockSignals(val)
    self.combo_box_maillage.blockSignals(val)
    self.combo_box_listeTypeExtraction.blockSignals(val)
    self.combo_box_listeTypeMultiEcho.blockSignals(val)

  def checkDisponibilite(self):
    if plateforme == "win":
      idx = self.combo_box_Mode.currentIndex() 
      if idx == 2 or idx == 4:
        QMessageBox.critical(self, u"Attention", u"Le traitement de fichiers ou projets Riegl n'est pas disponible sous Windows. Linux vaincra !")
      
      
      
  def OnModeChanged(self):
    self.blockSignalsComboBox(True)
    self.enableAll()
    self.checkDisponibilite()
    
    if self.combo_box_Mode.currentIndex() == 0:
      self.text_nomSortieDensite.setVisible(False)
      self.text_nomMaket.setVisible(False)
      self.text_inputFichierXYZ.setVisible(False)
      self.text_inputFichierRXP.setVisible(False)
      self.text_nomScan.setVisible(False)
      self.text_nombreTirs.setVisible(False)

      self.text_projRiegl.setVisible(False)
      self.text_dossierOutput.setVisible(True)
      self.enableXYZMinMax(True)

      self.combo_box_listeScan.setVisible(False)
      self.combo_box_maillage.setVisible(True)
      self.combo_box_listeTypeExtraction.setVisible(False)
      self.combo_box_listeTypeMultiEcho.setVisible(False)
      self.combo_box_ponderation.setVisible(False)

      self.enableFiltre(False)
      self.enableFiltreTheta(False)

    elif self.combo_box_Mode.currentIndex() == 1:
      self.text_Resolution.setVisible(False)
      self.text_nomSortieDensite.setVisible(False)
      self.text_nomMaket.setVisible(False)
      self.text_nomScan.setVisible(False)
      self.text_nombreTirs.setVisible(False)
      self.enableXYZMinMax(False)
      self.enableFiltre(False)
      self.enableFiltreTheta(False)

#      self.text_projRiegl.setVisible(True)
      self.text_dossierOutput.setVisible(True)
#      self.text_inputFichierXYZ.setVisible(True)
#      self.text_inputFichierRXP.setVisible(True)

      self.combo_box_maillage.setVisible(False)
      self.combo_box_listeScan.setVisible(True)
      self.combo_box_listeTypeExtraction.setVisible(False)
      self.combo_box_listeTypeMultiEcho.setVisible(False)
      self.combo_box_ponderation.setVisible(False)

    elif self.combo_box_Mode.currentIndex() == 2:
      self.text_nomScan.setVisible(False)
      self.text_nombreTirs.setVisible(False)
      self.text_Resolution.setVisible(True)
      self.text_nomSortieDensite.setVisible(True)
      self.text_nomMaket.setVisible(True)

#      self.text_projRiegl.setVisible(True)
      self.text_dossierOutput.setVisible(True)

      self.enableXYZMinMax(True)

      self.enableFiltre(True)
      self.enableFiltreTheta(True)

      self.combo_box_maillage.setVisible(True)
      self.combo_box_listeScan.setVisible(True)
      self.combo_box_listeTypeExtraction.setVisible(False)
      self.combo_box_listeTypeMultiEcho.setVisible(True)
      self.combo_box_ponderation.setVisible(True)


    elif self.combo_box_Mode.currentIndex() == 3:
      self.text_dossierOutput.setVisible(True)
      self.enableXYZMinMax(True)
      self.text_Resolution.setVisible(True)
      self.text_nomSortieDensite.setVisible(True)
      self.text_nomMaket.setVisible(True)
      self.text_inputFichierXYZ.setVisible(True)
      self.text_inputFichierRXP.setVisible(False)
      self.text_nomScan.setVisible(False)
      self.text_nombreTirs.setVisible(False)
      self.text_projRiegl.setVisible(False)

      self.combo_box_maillage.setVisible(True)
      self.combo_box_listeScan.setVisible(False)
      self.combo_box_listeTypeExtraction.setVisible(False)
      self.combo_box_listeTypeMultiEcho.setVisible(True)
      self.combo_box_ponderation.setVisible(True)

      self.enableFiltre(False)
      self.enableFiltreTheta(True)

    elif self.combo_box_Mode.currentIndex() == 4:
      self.text_Resolution.setVisible(False)
      self.text_nomSortieDensite.setVisible(False)
      self.text_nomMaket.setVisible(False)
      self.combo_box_listeTypeExtraction.setVisible(True)
      if self.combo_box_listeTypeExtraction.currentIndex() == 1:
        self.enableXYZMinMax(True)
      else:
        self.enableXYZMinMax(False)

      self.manageInputTXTRXP()
      self.text_inputFichierXYZ.setVisible(False)
      self.text_inputFichierRXP.setVisible(False)
      self.text_nombreTirs.setVisible(True)
      self.text_projRiegl.setVisible(True)
      self.text_dossierOutput.setVisible(True)

      self.combo_box_maillage.setVisible(False)
      self.combo_box_listeScan.setVisible(True)
      self.combo_box_listeTypeMultiEcho.setVisible(True)
      self.combo_box_ponderation.setVisible(False)

      self.enableFiltre(False)
      self.enableFiltreTheta(True)

    elif self.combo_box_Mode.currentIndex() == 5:
      self.text_dossierOutput.setVisible(True)
      self.enableXYZMinMax(True)
      self.text_Resolution.setVisible(True)
      self.text_nomSortieDensite.setVisible(True)
      self.text_nomMaket.setVisible(True)
      self.text_inputFichierXYZ.setVisible(True)
      self.text_inputFichierRXP.setVisible(False)
      self.text_nomScan.setVisible(False)
      self.text_nombreTirs.setVisible(False)
      self.text_projRiegl.setVisible(False)

      self.combo_box_maillage.setVisible(False)
      self.combo_box_listeScan.setVisible(False)
      self.combo_box_listeTypeExtraction.setVisible(False)
      self.combo_box_listeTypeMultiEcho.setVisible(True)
      self.combo_box_ponderation.setVisible(True)

      self.enableFiltre(False)
      self.enableFiltreTheta(True)

    self.OnTypePonderationChanged()
    self.oniFiltreChanged()
    self.ondFiltreChanged()
    self.onThetaFiltreChanged()
    self.onMatriceSOPSupplementaireChanged()
    self.OnListeScanChanged()
    self.blockSignalsComboBox(False)

  def enableAll(self):
      self.text_dossierOutput.setVisible(True)
      self.enableXYZMinMax(True)
      self.text_Resolution.setVisible(True)
      self.text_nomSortieDensite.setVisible(True)
      self.text_nomMaket.setVisible(True)
      self.text_inputFichierXYZ.setVisible(True)
      self.text_inputFichierRXP.setVisible(True)
      self.text_nomScan.setVisible(True)
      self.text_nombreTirs.setVisible(True)
      self.text_projRiegl.setVisible(True)

      self.combo_box_maillage.setVisible(True)
      self.combo_box_listeScan.setVisible(True)
      self.combo_box_ponderation.setVisible(True)
      self.combo_box_listeTypeExtraction.setVisible(True)
      self.combo_box_listeTypeMultiEcho.setVisible(True)


  def OnListeScanChanged(self):
    if self.combo_box_Mode.currentIndex() == 3:
#      if self.combo_box_listeScan.currentIndex() == 0:
#        self.text_inputFichierXYZ.setVisible(False)
#        self.text_inputFichierRXP.setVisible(False)
#        self.text_projRiegl.setVisible(True)
#        self.text_nomScan.setVisible(True)
#
#      elif self.combo_box_listeScan.currentIndex() == 1:
#        self.text_inputFichierXYZ.setVisible(False)
#        self.text_inputFichierRXP.setVisible(False)
#        self.text_projRiegl.setVisible(True)
#        self.text_nomScan.setVisible(True)
#
#      elif self.combo_box_listeScan.currentIndex() == 2:
        self.text_inputFichierXYZ.setVisible(True)
        self.text_inputFichierRXP.setVisible(False)
        self.text_projRiegl.setVisible(False)
        self.text_nomScan.setVisible(False)

#      elif self.combo_box_listeScan.currentIndex() == 3:
#        self.text_inputFichierXYZ.setVisible(False)
#        self.text_inputFichierRXP.setVisible(True)
#        self.text_projRiegl.setVisible(False)
#        self.text_nomScan.setVisible(False)

    if self.combo_box_Mode.currentIndex() == 2 or self.combo_box_Mode.currentIndex() == 4:
      if self.combo_box_listeScan.currentIndex() == 0:
        self.text_projRiegl.setVisible(True)
        self.text_inputFichierXYZ.setVisible(False)
        self.text_inputFichierRXP.setVisible(False)
        self.text_nomScan.setVisible(False)
      elif self.combo_box_listeScan.currentIndex() == 1:
        self.text_projRiegl.setVisible(True)
        self.text_inputFichierXYZ.setVisible(False)
        self.text_inputFichierRXP.setVisible(False)
        self.text_nomScan.setVisible(True)
      elif self.combo_box_listeScan.currentIndex() == 2:
        self.text_projRiegl.setVisible(False)
        self.text_inputFichierXYZ.setVisible(False)
        self.text_inputFichierRXP.setVisible(False)
        self.text_nomScan.setVisible(False)
        self.text_dossierOutput.setVisible(False)
        self.combo_box_maillage.setVisible(False)
        self.text_nombreTirs.setVisible(False)
        self.text_Resolution.setVisible(False)
        self.text_nomSortieDensite.setVisible(False)
        self.text_nomMaket.setVisible(False)
        self.combo_box_listeTypeMultiEcho.setVisible(False)
      elif self.combo_box_listeScan.currentIndex() == 3:
        self.text_projRiegl.setVisible(False)
        self.text_inputFichierXYZ.setVisible(False)
        self.text_inputFichierRXP.setVisible(True)
        self.text_nomScan.setVisible(False)

  def ecritConsole(self, msg):
    if msg:
      self.textConsole.append(unicode(msg))

  def enableXYZMinMax(self, test):
    self.text_Xmin.setVisible(test)
    self.text_Ymin.setVisible(test)
    self.text_Zmin.setVisible(test)

    self.text_Xmax.setVisible(test)
    self.text_Ymax.setVisible(test)
    self.text_Zmax.setVisible(test)

  def enableFiltre(self, test):
    self.cbox_iFiltre.setVisible(test)
    self.text_iMin.setVisible(test)
    self.text_iMax.setVisible(test)
    self.cbox_dFiltre.setVisible(test)
    self.text_dMin.setVisible(test)
    self.text_dMax.setVisible(test)

  def enableFiltreTheta(self, test):
    self.cbox_thetaFiltre.setVisible(test)
    self.text_tetaMin.setVisible(test)
    self.text_tetaMax.setVisible(test)

  def oniFiltreChanged(self):
    if self.cbox_iFiltre.checkState():
      self.text_iMin.setVisible(True)
      self.text_iMax.setVisible(True)
    else:
      self.text_iMin.setVisible(False)
      self.text_iMax.setVisible(False)

  def onMatriceSOPSupplementaireChanged(self):
    if self.cbox_matSOP.checkState():
      self.text_cheminMatSop.setVisible(True)
    else:
      self.text_cheminMatSop.setVisible(False)

  def ondFiltreChanged(self):
    if self.cbox_dFiltre.checkState():
      self.text_dMin.setVisible(True)
      self.text_dMax.setVisible(True)
    else:
      self.text_dMin.setVisible(False)
      self.text_dMax.setVisible(False)

  def onThetaFiltreChanged(self):
    if self.cbox_thetaFiltre.checkState():
      self.text_tetaMin.setVisible(True)
      self.text_tetaMax.setVisible(True)
    else:
      self.text_tetaMin.setVisible(False)
      self.text_tetaMax.setVisible(False)

  def updateIHMDepuisProprietes(self):
    self.text_Resolution.setText(str(self.properties['resolution']))
    self.text_nomSortieDensite.setText(str(self.properties['fichierSortieDensite']))
    self.text_nomMaket.setText(str(self.properties['fichierSortieDensiteFormatDART']))
    self.text_inputFichierXYZ.setText(str(self.properties['fichierXYZ']))
    self.text_inputFichierRXP.setText(str(self.properties['fichierRXP']))
    self.text_nomScan.setText(str(self.properties['nomScan']))
    self.text_nombreTirs.setText(str(self.properties['nombreMaxShotsParfichier']))
    self.text_projRiegl.setText(str(self.properties['cheminProjetRiegl']))
    self.text_dossierOutput.setText(str(self.properties['dossierOutput']))
    self.text_FichierPonderation.setText(str(self.properties['fichierPonderation']))

    self.text_Xmin.setText(str(self.properties['pointMailleMin.x']))
    self.text_Ymin.setText(str(self.properties['pointMailleMin.y']))
    self.text_Zmin.setText(str(self.properties['pointMailleMin.z']))

    self.text_Xmax.setText(str(self.properties['pointMailleMax.x']))
    self.text_Ymax.setText(str(self.properties['pointMailleMax.y']))
    self.text_Zmax.setText(str(self.properties['pointMailleMax.z']))

    if 'iFiltre' in self.properties.keys():
      if int(self.properties['iFiltre']) :  state = Qt.Checked
      else: state = Qt.Unchecked
    else: state = Qt.Unchecked
    self.cbox_iFiltre.setChecked(state)
    self.oniFiltreChanged()
    self.text_iMin.setText(str(self.properties['filtre.iMin']))
    self.text_iMax.setText(str(self.properties['filtre.iMax']))

    if 'dFiltre' in self.properties.keys():
      if int(self.properties['dFiltre']) :  state = Qt.Checked
      else: state = Qt.Unchecked
    else: state = Qt.Unchecked
    self.cbox_dFiltre.setChecked(state)
    self.ondFiltreChanged()
    self.text_dMin.setText(str(self.properties['filtre.dMin']))
    self.text_dMax.setText(str(self.properties['filtre.dMax']))

    if 'appliquerMatriceSOPSupplementaire' in self.properties.keys():
      if int(self.properties['appliquerMatriceSOPSupplementaire']) :  state = Qt.Checked
      else: state = Qt.Unchecked
    else: state = Qt.Unchecked
    self.cbox_matSOP.setChecked(state)
    self.onMatriceSOPSupplementaireChanged()
    self.text_cheminMatSop.setText(str(self.properties['cheminFichierMatriceSOPSupplementaire']))

    if 'thetaFiltre' in self.properties.keys():
      if int(self.properties['thetaFiltre']) :  state = Qt.Checked
      else: state = Qt.Unchecked
    else: state = Qt.Unchecked
    self.cbox_thetaFiltre.setChecked(state)
    self.onThetaFiltreChanged()
    self.text_tetaMin.setText(str(self.properties['filtre.thetaMin']))
    self.text_tetaMax.setText(str(self.properties['filtre.thetaMax']))

    if self.properties['typeExecution'] != "":
      self.combo_box_Mode.setCurrentIndex(int(self.properties['typeExecution']))
    if self.properties['typeMaillage'] != "":
      self.combo_box_maillage.setCurrentIndex(int(self.properties['typeMaillage']))
    if self.properties['methodePonderationEchos'] != "":
      self.combo_box_ponderation.setCurrentIndex(int(self.properties['methodePonderationEchos']))

    if self.properties['listeScans'] != "":
      self.combo_box_listeScan.setCurrentIndex(int(self.properties['listeScans']))
    if self.properties['typeExtractionPoints'] != "":
      self.combo_box_listeTypeExtraction.setCurrentIndex(int(self.properties['typeExtractionPoints']))
    if self.properties['typeMultiEcho'] != "":
      self.combo_box_listeTypeMultiEcho.setCurrentIndex(int(self.properties['typeMultiEcho']))


  def updateFichierProprietesDepuisIHM(self):
    if self.text_Resolution.text() != "" and self.text_Resolution.isEnabled():
      self.properties['resolution'] = self.text_Resolution.text()
    else: del self.properties['resolution']

    if self.text_nomSortieDensite.text() != "" and self.text_nomSortieDensite.isEnabled():
      self.properties['fichierSortieDensite'] = self.text_nomSortieDensite.text()
    else: del self.properties['fichierSortieDensite']
#    
    if self.text_nomMaket.text() != "" and self.text_nomMaket.isEnabled():
      self.properties['fichierSortieDensiteFormatDART'] = self.text_nomMaket.text()
    else: del self.properties['fichierSortieDensiteFormatDART']
#    
    if self.text_inputFichierXYZ.text() != "" and self.text_inputFichierXYZ.isEnabled():
      self.properties['fichierXYZ'] = self.text_inputFichierXYZ.text()
    else: del self.properties['fichierXYZ']

    if self.text_inputFichierRXP.text() != "" and self.text_inputFichierRXP.isEnabled():
      self.properties['fichierRXP'] = self.text_inputFichierRXP.text()
    else: del self.properties['fichierRXP']

    if self.text_nomScan.text() != "" and self.text_nomScan.isEnabled():
      self.properties['nomScan'] = self.text_nomScan.text()
    else: del self.properties['nomScan']

    if self.text_nombreTirs.text() != "" and self.text_nombreTirs.isEnabled():
      self.properties['nombreMaxShotsParfichier'] = self.text_nombreTirs.text()
    else: del self.properties['nombreMaxShotsParfichier']

    if self.text_projRiegl.text() != "" and self.text_projRiegl.isEnabled():
      self.properties['cheminProjetRiegl'] = self.text_projRiegl.text()
    else: del self.properties['cheminProjetRiegl']

    if self.text_dossierOutput.text() != "" and self.text_dossierOutput.isEnabled():
      self.properties['dossierOutput'] = self.text_dossierOutput.text()
    else: del self.properties['dossierOutput']

    if self.text_Xmin.text() != "" and self.text_Xmin.isEnabled():
      self.properties['pointMailleMin.x'] = self.text_Xmin.text()
    else: del self.properties['pointMailleMin.x']

    if self.text_Ymin.text() != "" and self.text_Ymin.isEnabled():
      self.properties['pointMailleMin.y'] = self.text_Ymin.text()
    else: del self.properties['pointMailleMin.y']

    if self.text_Zmin.text() != "" and self.text_Zmin.isEnabled():
      self.properties['pointMailleMin.z'] = self.text_Zmin.text()
    else: del self.properties['pointMailleMin.z']

    if self.text_Xmax.text() != "" and self.text_Xmax.isEnabled():
      self.properties['pointMailleMax.x'] = self.text_Xmax.text()
    else: del self.properties['pointMailleMax.x']

    if self.text_Ymax.text() != "" and self.text_Ymax.isEnabled():
      self.properties['pointMailleMax.y'] = self.text_Ymax.text()
    else: del self.properties['pointMailleMax.y']

    if self.text_Zmax.text() != "" and self.text_Zmax.isEnabled():
      self.properties['pointMailleMax.z'] = self.text_Zmax.text()
    else: del self.properties['pointMailleMax.z']

    if self.text_FichierPonderation.text() != "" and self.text_FichierPonderation.isEnabled():
      self.properties['fichierPonderation'] = self.text_FichierPonderation.text()
    else: del self.properties['fichierPonderation']

    if self.cbox_iFiltre.isEnabled():
      if self.cbox_iFiltre.checkState() == Qt.Checked:
        self.properties['iFiltre'] = 1
      else:
        self.properties['iFiltre'] = 0
      if self.cbox_iFiltre.text() != False and self.cbox_iFiltre.isEnabled():
        if self.text_iMin.text() != "" and self.text_iMin.isEnabled():
          self.properties['filtre.iMin'] = self.text_iMin.text()
        else: del self.properties['filtre.iMin']

        if self.text_iMax.text() != "" and self.text_iMax.isEnabled():
          self.properties['filtre.iMax'] = self.text_iMax.text()
        else: del self.properties['filtre.iMax']
    else: del self.properties['iFiltre']

    if self.cbox_dFiltre.isEnabled():
      if self.cbox_dFiltre.checkState() == Qt.Checked:
        self.properties['dFiltre'] = 1
      else:
        self.properties['dFiltre'] = 0
      if self.cbox_dFiltre.text() != False:
        if self.text_dMin.text() != "" and self.text_dMin.isEnabled():
          self.properties['filtre.dMin'] = self.text_dMin.text()
        else: del self.properties['filtre.dMin']

        if self.text_dMax.text() != "" and self.text_dMax.isEnabled():
          self.properties['filtre.dMax'] = self.text_dMax.text()
        else: del self.properties['filtre.dMax']
    else: del self.properties['dFiltre']

    if self.cbox_thetaFiltre.isEnabled():
      if self.cbox_thetaFiltre.checkState() == Qt.Checked:
        self.properties['thetaFiltre'] = 1
      else:
        self.properties['thetaFiltre'] = 0
      if self.cbox_dFiltre.text() != False:
        if self.text_tetaMin.text() != "" and self.text_tetaMin.isEnabled():
          self.properties['filtre.thetaMin'] = self.text_tetaMin.text()
        else: del self.properties['filtre.thetaMin']

        if self.text_tetaMax.text() != "" and self.text_tetaMax.isEnabled():
          self.properties['filtre.thetaMax'] = self.text_tetaMax.text()
        else: del self.properties['filtre.thetaMax']
    else: del self.properties['thetaFiltre']
   
    if self.cbox_matSOP.isEnabled():
      self.properties['appliquerMatriceSOPSupplementaire'] = int(self.cbox_matSOP.checkState())
      if self.cbox_matSOP.text() != False:
        if self.text_cheminMatSop.text() != "" and self.cbox_matSOP.isEnabled():
          self.properties['cheminFichierMatriceSOPSupplementaire'] = self.text_cheminMatSop.text()
        else: del self.properties['cheminFichierMatriceSOPSupplementaire']



    if self.combo_box_Mode.isEnabled():
      self.properties['typeExecution'] = self.combo_box_Mode.currentIndex()
    else: del self.properties['typeExecution']

    if self.combo_box_maillage.isEnabled():
      self.properties['typeMaillage'] = self.combo_box_maillage.currentIndex()
    else: del self.properties['typeMaillage']

    if self.combo_box_ponderation.isEnabled():
      self.properties['methodePonderationEchos'] = self.combo_box_ponderation.currentIndex()
    else: del self.properties['methodePonderationEchos']

    if self.combo_box_listeScan.isEnabled():
      self.properties['listeScans'] = self.combo_box_listeScan.currentIndex()
    else: del self.properties['listeScans']

    if self.combo_box_listeTypeExtraction.isEnabled():
      self.properties['typeExtractionPoints'] = self.combo_box_listeTypeExtraction.currentIndex()
    else: del self.properties['typeExtractionPoints']

    if self.combo_box_listeTypeMultiEcho.isEnabled():
      self.properties['typeMultiEcho'] = self.combo_box_listeTypeMultiEcho.currentIndex()
    else: del self.properties['typeMultiEcho']


  def previewReport(self, path, modal = False):
    """Preview the passed report (txt or pdf) file in the default viewer."""

    if not os.path.isfile(path):
      self.textConsole.append("Fichier non disponible (" + path + ")")
      return
    reportType = path.split(".")[-1]
    if reportType == "txt":
      viewers = ("gedit", "kate", "firefox", "mozilla-firefox", "chromium")
    elif reportType == "pdf":
      viewers = ("okular", "gpdf", "kpdf", "evince", "acroread", "xpdf", "firefox",
        "mozilla-firefox")
    else:
      raise ValueError("Unknown report type '%s'." % reportType)

    viewer = None
    for v in viewers:
      r = os.system("which %s 1> /dev/null 2> /dev/null" % v)
      if r == 0:
        viewer = v
        break

    if viewer:
      if modal:
        subprocess.call((viewer, path))
      else:
        subprocess.Popen((viewer, path))


  def OnSearch_projRiegl(self):
    dir = self.text_projRiegl.text()
    if dir == "":
      dir = unicode(self.text_repertoireDefaut.text())
    dossier = QFileDialog.getExistingDirectory(self, "Dossier du projet RIEGL", dir)
    if dossier:
      self.text_projRiegl.setText(dossier)

  def OnSearch_dossierOutput(self):
    dir = self.text_dossierOutput.text()
    if dir == "":
      dir = unicode(self.text_repertoireDefaut.text())
    dossier = QFileDialog.getExistingDirectory(self, "Dossier des résultats", dir, QFileDialog.ShowDirsOnly)
    if dossier:
      self.text_dossierOutput.setText(dossier)
      self.toolbox[0].setTextDossierOutput(self.text_dossierOutput)

  def OnSearch_fichierXYZ(self):
    dir = self.text_inputFichierXYZ.text()
    if dir == "":
      dir = unicode(self.text_repertoireDefaut.text())
    else:
      dir = QString(os.path.dirname(unicode(dir)))
    fichier = QFileDialog.getOpenFileName(self, "Fichier XYZ", dir)
    if fichier:
      self.text_inputFichierXYZ.setText(fichier)

  def OnSearch_fichierPonderation(self):
    dir = self.getRepertoire(self.text_FichierPonderation)
    fichier = QFileDialog.getOpenFileName(self, "Fichier de ponderation", dir)
    if fichier:
      self.text_FichierPonderation.setText(fichier)

  def OnSearch_fichierRXP(self):
    dir = self.text_inputFichierRXP.text()
    if dir == "":
      dir = unicode(self.text_repertoireDefaut.text())
    else:
      dir = QString(os.path.dirname(unicode(dir)))
    fichier = QFileDialog.getOpenFileName(self, "Fichier RXP", dir, filter = "*.rxp")
    if fichier:
      self.text_inputFichierRXP.setText(fichier)

  def OnSearch_matSOPSupplementaire(self):
    dir = self.getRepertoire(self.text_cheminMatSop)
    fichier = QFileDialog.getOpenFileName(self, u"Fichier SOP Supplémentaire", dir)
    if fichier:
      self.text_cheminMatSop.setText(fichier)

  def OnSearch_fichierLog(self):
    fic = self.text_cheminLog.text()
    if fic == "":
      dir = unicode(self.text_repertoireDefaut.text())
    else:
      dir = QString(os.path.dirname(unicode(fic)))

    fichier = QFileDialog.getSaveFileName(self, "Fichier log", dir)
    if fichier:
      self.text_cheminLog.setText(fichier)

  def OnSearch_cheminExecutable(self):
    fic = self.text_cheminExec.text()
    if fic == "":
      dir = unicode(self.text_repertoireDefaut.text())
    else:
      dir = QString(os.path.dirname(unicode(fic)))

    fichier = QFileDialog.getOpenFileName(self, u"Choisissez l'executable", dir)
    if fichier:
      self.text_cheminExec.setText(fichier)

  def OnSearch_repertoireDefaut(self):
    fic = self.text_repertoireDefaut.text()
    if fic == "":
      dir = unicode(self.text_repertoireDefaut.text())
    else:
      dir = QString(os.path.dirname(unicode(fic)))
    fichier = QFileDialog.getExistingDirectory(self, u"Repertoire par défaut", dir)
    if fichier:
      self.text_repertoireDefaut.setText(fichier)


  def updateOptions(self, iMin, iMax, dMin, dMax, zMin, zMax):
    self.text_iMin_visu.setText(str(iMin))
    self.text_iMax_visu.setText(str(iMax))
    self.text_dMin_visu.setText(str(dMin))
    self.text_dMax_visu.setText(str(dMax))
    self.text_zMin_visu.setText(str(zMin))
    self.text_zMax_visu.setText(str(zMax))

  def getRepertoireDensite(self, champsTexte = ""):
    if champsTexte:
      fic = champsTexte.text()
    else:
      fic = self.text_dossierOutput.text()
    if fic == "":
      dir = self.text_dossierOutput.text()
      if dir == "":
        dir = unicode(self.text_repertoireDefaut.text())
      else:
        dir = dir + "/densite/"
        if not os.path.isdir(dir):
          dir = self.text_dossierOutput.text()
    else:
      dir = os.path.dirname(unicode(fic) + "/") + "/densite/"
      if not os.path.isdir(dir):
        dir = os.path.dirname(unicode(fic) + "/")
    return dir

  def getRepertoirePoints(self, champsTexte = ""):
    if champsTexte:
      fic = champsTexte.text()
    else:
      fic = self.text_dossierOutput.text()
    if not fic:
      dir = self.text_dossierOutput.text()
      if dir == "":
        dir = unicode(self.text_repertoireDefaut.text())
      else:
        dir = dir + "/points/"
        if not os.path.isdir(dir):
          dir = self.text_dossierOutput.text()
    else:
      dir = os.path.dirname(unicode(fic) + "/") + "/points/"
      if not os.path.isdir(dir):
        dir = os.path.dirname(unicode(fic))
    return dir

  def getRepertoireCourant(self):
    if self.properties.cheminFichier != "":
      dir = os.path.dirname(self.properties.cheminFichier)
      if dir == "":
        dir = unicode(self.text_repertoireDefaut.text())
    else:
        dir = unicode(self.text_repertoireDefaut.text())
    return dir

  def getRepertoire(self, champsTexte):
    fic = champsTexte.text()
    if fic == "":
      dir = champsTexte.text()
      if dir == "":
        dir = unicode(self.text_repertoireDefaut.text())
    else:
      dir = QString(os.path.dirname(unicode(fic)))
    return dir

  def OnSearch_fichierPoints_visu(self):
    dir = self.getRepertoirePoints(self.text_fichierPoints_visu)
    fichier = QFileDialog.getOpenFileName(self, u"Fichier de points à visualiser", dir)
    if fichier:
      self.text_fichierPoints_visu.setText(fichier)

  def OnSearch_fichierPoints_filtrer(self):
    dir = self.getRepertoirePoints(self.text_fichierPoints_filtrer)
    fichier = QFileDialog.getOpenFileName(self, "Fichier de points à filrer", dir)
    if fichier:
      self.text_fichierPoints_filtrer.setText(fichier)

  def visualiserMaille(self):
    dir = self.getRepertoireDensite()
    fichiers = QFileDialog.getOpenFileNames(self, u"Fichier de densite à visualiser", dir)
    for fic in fichiers:
      if self.maille.nomFichier != unicode(fic):
        self.maille = getMaillage(fic)
      Voxel.seuilCalculDensite = float(self.text_seuilVisuDensite.text())
      self.maille.afficheDensite(_show = False)
    #mlab.show()



  def visualiserMailleFiltree(self):
    dir = self.getRepertoireDensite()
    fichiers = QFileDialog.getOpenFileNames(self, u"Fichier de densite à visualiser", dir)
    show = False
    for i, fic in enumerate(fichiers):
      if self.maille.nomFichier != unicode(fic):
        self.maille = getMaillage(fic)
      Voxel.seuilCalculDensite = float(self.text_seuilVisuDensite.text())
      if i == len(fichiers) - 1:
        show = True
      self.maille.afficheDensiteEtCellulesPleines(_show = show)

  def fusionnerMaillages(self):
    dir = self.getRepertoireDensite(self.text_dossierOutput)
    fichiers = QFileDialog.getOpenFileNames(self, u"Selectionner les fichiers de densité à fusionner", dir)
    if fichiers:
      if len(fichiers) > 1:
        maille = getMaillage(fichiers[0])
        for i in range(1, len(fichiers)):
          maille2 = getMaillage(fichiers[i])
          maille.fusionnerMaillages(maille2)
        maille.ecrireFichierDensite(fichiers[0] + "_fusionnes.txt")
        maille.ecrireFichierMaket(fichiers[0] + "_fusionnes_maket.txt")

  def fusionnerFichiersPoints(self):
    dir = self.getRepertoirePoints(self.text_dossierOutput)
    fichiers = QFileDialog.getOpenFileNames(self, u"Selectionner les fichiers de points à fusionner", dir)
    if fichiers:
      fusionnerFichiersPoints(fichiers)


class toolBox(QToolBox, ihmToolbox.Ui_ToolBox):
  def __init__(self, getRepertoirePoints, getRepertoireDensite, textdossierOutput, ecritConsole, parent = None):
    super(toolBox, self).__init__(parent)

    self.getRepertoirePoints = getRepertoirePoints
    self.getRepertoireDensite = getRepertoireDensite
    self.textDossierOutput = textdossierOutput
    self.ecritConsole = ecritConsole

    self.setupUi(self)
    self.initActions()

    self.maille = Maille()
    self.visuPoints = Visualisateur()

    self.show()

  def setTextDossierOutput(self, textdossierOutput):
    self.textDossierOutput = textdossierOutput


  def initActions(self):
    self.connect(self.boutonFichierPoints, SIGNAL("released()"), self.OnSearchFichierPoints);
    self.connect(self.boutonFichierMNT, SIGNAL("released()"), self.OnSearchFichierMNT);
    self.connect(self.filtrerPointSurCoucheDZ, SIGNAL("released()"), self.OnExectuerfiltrageDZMNT);

    self.connect(self.boutonfusionDensite, SIGNAL("released()"), self.fusionnerMaillages);
    self.connect(self.boutonfusionDensite_pondere, SIGNAL("released()"), self.fusionnerMaillages_pondere);
    self.connect(self.boutonfusionPoints, SIGNAL("released()"), self.fusionnerFichiersPoints);
    self.connect(self.boutonFichierPoints_filtrer, SIGNAL("released()"), self.OnSearch_fichierPoints_filtrer);
    self.connect(self.boutonFichierPoints_projeter, SIGNAL("released()"), self.OnSearch_fichierPoints_projeter);
    self.connect(self.boutonProjeterDensite, SIGNAL("released()"), self.projeterMaille);
    self.connect(self.boutonProjeterPoints, SIGNAL("released()"), self.projeterPoints);
    self.connect(self.boutonFiltrerPoints, SIGNAL("released()"), self.filtrerPoints);
    self.connect(self.boutonCalculerMNT, SIGNAL("released()"), self.calculerMNT);
#    self.connect(self.boutonWatershed, SIGNAL("released()"), self.getWatershed);   
    self.connect(self.boutonFichierPoints_visu, SIGNAL("released()"), self.OnSearch_fichierPoints_visu);

    self.connect(self.boutonLancerVisuDensite, SIGNAL("released()"), self.visualiserMaille);
    self.connect(self.boutonLancerVisuNdedans, SIGNAL("released()"), self.visualiserNdedans);
    self.connect(self.boutonLancerVisuNApres, SIGNAL("released()"), self.visualiserNapres);
    self.connect(self.boutonLancerVisuNTotal, SIGNAL("released()"), self.visualiserNtotal);
    self.connect(self.boutonLancerVisuPoints_i, SIGNAL("released()"), self.visualiserPointsIntensite);
    self.connect(self.boutonLancerVisuPoints_d, SIGNAL("released()"), self.visualiserPointsDeviation);
    self.connect(self.boutonLancerVisuPoints_o, SIGNAL("released()"), self.visualiserPointsOrdre);

    self.connect(self.boutonLancerVisuALS, SIGNAL("released()"), self.visualiserFichierPointsALS);
    self.connect(self.boutonLancerVisuDensite_distribution, SIGNAL("released()"), self.visualiserDistributionDensite);
    self.connect(self.boutonLancerVisuLAS, SIGNAL("released()"), self.visualiserFichierPointsLAS);
    self.connect(self.boutonLancerVisuDensite_3classes, SIGNAL("released()"), self.visualiserMailleFiltree);
    self.connect(self.boutonLancerVisuDensite_profil, SIGNAL("released()"), self.visualiserProfilLAI);

  def OnSearchFichierPoints(self):
    dir = self.getRepertoirePoints(self.textDossierOutput)
    fichier = QFileDialog.getOpenFileName(self, "Fichier de points à filtrer", dir)
    if fichier:
      self.text_fichierPoints_filtrer.setText(fichier)

  def OnSearchFichierMNT(self):
    dir = self.getRepertoireDensite(self.textDossierOutput)
    fichier = QFileDialog.getOpenFileName(self, "Fichier MNT", dir)
    if fichier:
      self.text_fichierMNT.setText(fichier)

  def OnExectuerfiltrageDZMNT(self):
    if self.text_fichierPoints_filtrer.text() == "":
      self.OnSearchFichierPoints()

    altMoy = float(self.text_altitudeMoy.text())
    dz = float(self.text_dz.text())
    filtrerFichierPointsSurCouchedZAvecMNT(self.text_fichierPoints_filtrer.text(), altMoy, dz, self.text_fichierMNT.text())

  def OnSearch_fichierPoints_visu(self):
    dir = self.getRepertoirePoints(self.text_fichierPoints_visu)
    fichier = QFileDialog.getOpenFileName(self, u"Fichier de points à visualiser", dir)
    if fichier:
      self.text_fichierPoints_visu.setText(fichier)

  def OnSearch_fichierPoints_projeter(self):
    dir = self.getRepertoirePoints(self.text_fichierPoints_projeter)
    fichier = QFileDialog.getOpenFileName(self, "Fichier de points à visualiser", dir)
    if fichier:
      self.text_fichierPoints_projeter.setText(fichier)

  def OnSearch_fichierPoints_filtrer(self):
    dir = self.getRepertoirePoints(self.text_fichierPoints_filtrer)
    fichier = QFileDialog.getOpenFileName(self, "Fichier de points à filtrer", dir)
    if fichier:
      self.text_fichierPoints_filtrer.setText(fichier)

  def visualiserMaille(self):
    dir = self.getRepertoireDensite()
    fichiers = QFileDialog.getOpenFileNames(self, u"Fichier de densité à visualiser", dir)
    for fic in fichiers:
      if self.maille.nomFichier != unicode(fic):
        self.maille = getMaillage(fic)
      Voxel.seuilCalculDensite = float(self.text_seuilVisuDensite.text())
      self.maille.afficheDensite()

  def visualiserMailleFiltree(self):
    dir = self.getRepertoireDensite()
    print dir
    fichiers = QFileDialog.getOpenFileNames(self, u"Fichier de densité à visualiser", dir)
    show = False
    for i, fic in enumerate(fichiers):
      if self.maille.nomFichier != unicode(fic):
        self.maille = getMaillage(fic)
      Voxel.seuilCalculDensite = float(self.text_seuilVisuDensite.text())
      if i == len(fichiers) - 1:
        show = True
      self.maille.afficheDensiteEtCellulesPleines(_show = show)

  def fusionnerMaillages(self):
    dir = self.getRepertoireDensite(self.textDossierOutput)
    fichiers = QFileDialog.getOpenFileNames(self, u"Selectionner les fichiers de densité à fusionner", dir)
    if fichiers:
      print fichiers
      if len(fichiers) > 1:
        maille = getMaillage(fichiers[0])
        for i in range(1, len(fichiers)):
          maille2 = getMaillage(fichiers[i])
          maille.fusionnerMaillages(maille2, ponderation = False)
          del maille2
        maille.ecrireFichierDensite(fichiers[0] + "_fusionnes.txt")
        maille.ecrireFichierMaket(fichiers[0] + "_fusionnes_maket.txt")

  def visualiserNdedans(self):
    dir = self.getRepertoireDensite()
    fichiers = QFileDialog.getOpenFileNames(self, u"Fichier de densite à visualiser", dir)
    for fic in fichiers:
      if self.maille.nomFichier != unicode(fic):
        self.maille = getMaillage(fic)
      Voxel.seuilCalculDensite = float(self.text_seuilVisuDensite.text())    
      self.maille.afficheNdedans(_show = False)
    #mlab.show()

  def visualiserNtotal(self):
    dir = self.getRepertoireDensite()
    fichiers = QFileDialog.getOpenFileNames(self, u"Fichier de densite à visualiser", dir)
    for fic in fichiers:
      if self.maille.nomFichier != unicode(fic):
        self.maille = getMaillage(fic)
      Voxel.seuilCalculDensite = float(self.text_seuilVisuDensite.text())
      self.maille.afficheNtotal(_show = False)
    #mlab.show()

  def visualiserNapres(self):
    dir = self.getRepertoireDensite()
    fichiers = QFileDialog.getOpenFileNames(self, u"Fichier de densite à visualiser", dir)
    for fic in fichiers:
      if self.maille.nomFichier != unicode(fic):
        self.maille = getMaillage(fic)
      Voxel.seuilCalculDensite = float(self.text_seuilVisuDensite.text())
      self.maille.afficheNapres(_show = False)
    #mlab.show()

  def fusionnerFichiersPoints(self):
    dir = self.getRepertoirePoints(self.textDossierOutput)
    fichiers = QFileDialog.getOpenFileNames(self, u"Selectionner les fichiers de points à fusionner", dir)
    if fichiers:
      fusionnerFichiersPoints(fichiers)

  def fusionnerMaillages_pondere(self):
    dir = self.getRepertoireDensite(self.textDossierOutput)
    fichiers = QFileDialog.getOpenFileNames(self, u"Selectionner les fichiers de densité à fusionner", dir)
    if fichiers:
      print fichiers
      if len(fichiers) > 1:
        maille = getMaillage(fichiers[0])
        maille.fusionnerMaillagesListe(fichiers[1:])
        maille.ecrireFichierDensite(fichiers[0] + "_fusionnesPondere.txt")
        maille.ecrireFichierMaket(fichiers[0] + "_fusionnesPondere_maket.txt")

  def calculerMNT(self):
    dir = self.getRepertoireDensite(self.textDossierOutput)
    fic = QFileDialog.getOpenFileName(self, u"Selectionner le fichiers de densité pour calcul du MNT", dir)
    if fic:
#        image = QFileDialog.getSaveFileName(self, u"Selectionner le fichiers image pour sauvegarder le MNT", dir)

      if self.maille.nomFichier != unicode(fic):
        self.maille = getMaillage(fic)

      fichierMNT = unicode(fic) + "_MNT.txt"
      mnt = self.maille.calculeMNT(False, fichierMNT)
#        xi, yi = np.mgrid[0:maille.nbX:Voxel.resolution, 0:maille.nbY:Voxel.resolution]
#        afficheSurface3D(xi, yi, mnt)
#        plt.imshow(mnt)
#        plt.show()

  def visualiserPointsOrdre(self):
    fichier = self.text_fichierPoints_visu.text()
    if not fichier:
      self.OnSearch_fichierPoints_visu()

    fichier = self.text_fichierPoints_visu.text()
    if fichier:
      self.visuPoints.proportionPtsAfficher = int(self.text_proportionPoints_visu.text())
      self.visuPoints.afficheFichierPointsExtraits(fichier, typeVisu = "ordre", iMin = self.text_iMin_visu.text(), iMax = self.text_iMax_visu.text(), dMin = self.text_dMin_visu.text(), dMax = self.text_dMax_visu.text(), zMin = self.text_zMin_visu.text(), zMax = self.text_zMax_visu.text())
    else:
      self.ecritConsole("selectionnez un fichier à visualiser")

  def visualiserPointsIntensite(self):
    fichier = self.text_fichierPoints_visu.text()
    if not fichier:
      self.OnSearch_fichierPoints_visu()

    fichier = self.text_fichierPoints_visu.text()
    if fichier:
      self.visuPoints.proportionPtsAfficher = int(self.text_proportionPoints_visu.text())
      self.visuPoints.afficheFichierPointsExtraits(fichier, typeVisu = "intensite", iMin = float(self.text_iMin_visu.text()), iMax = float(self.text_iMax_visu.text()), dMin = float(self.text_dMin_visu.text()), dMax = float(self.text_dMax_visu.text()), zMin = float(self.text_zMin_visu.text()), zMax = float(self.text_zMax_visu.text()))
    else:
      self.ecritConsole("selectionnez un fichier à visualiser")

  def visualiserPointsDeviation(self):
    fichier = self.text_fichierPoints_visu.text()
    if not fichier:
      self.OnSearch_fichierPoints_visu()

    fichier = self.text_fichierPoints_visu.text()
    if fichier:
      self.visuPoints.proportionPtsAfficher = int(self.text_proportionPoints_visu.text())
      self.visuPoints.afficheFichierPointsExtraits(fichier, typeVisu = "deviation", iMin = self.text_iMin_visu.text(), iMax = self.text_iMax_visu.text(), dMin = self.text_dMin_visu.text(), dMax = self.text_dMax_visu.text(), zMin = self.text_zMin_visu.text(), zMax = self.text_zMax_visu.text())
    else:
      self.ecritConsole("selectionnez un fichier à visualiser")

  def filtrerPoints(self):
    fichier = self.text_fichierPoints_filtrer.text()
    if not fichier:
      self.OnSearch_fichierPoints_filtrer()

    fichier = self.text_fichierPoints_filtrer.text()
    if fichier:
      filtrerFichierPoints(fichier, iMin = self.text_iMin_visu_2.text(), iMax = self.text_iMax_visu_2.text(), dMin = self.text_dMin_visu_2.text(), dMax = self.text_dMax_visu_2.text(), zMin = self.text_zMin_visu_2.text(), zMax = self.text_zMax_visu_2.text())
    else:
      self.ecritConsole("selectionnez un fichier à filtrer")




  def projeterMaille(self):
    dir = self.getRepertoireDensite(self.textDossierOutput)
    fichier = QFileDialog.getOpenFileName(self, "Selectionner le fichier de densité à projeter", dir)
    if fichier:
      if self.maille.nomFichier != unicode(fichier):
        self.maille = getMaillage(fichier)
      self.maille.afficheProjectionZ(unicode(fichier) + "_projection.png")
    else:
      self.ecritConsole("selectionnez un fichier à projeter")

  def getWatershed(self):
    dir = self.getRepertoireDensite(self.textDossierOutput)
    fichier = QFileDialog.getOpenFileName(self, "Selectionner l'image à segmenter", dir)
    if fichier:
      appliqueWatershed(unicode(fichier))

  def projeterPoints(self):
    fichier = unicode(self.text_fichierPoints_projeter.text())
    if fichier:
      kwargs = {}
      kwargs["cheminFichier"] = fichier
      kwargs["resolution"] = float(self.text_resolution_projeter.text())
      kwargs["pointMin_x"] = float(self.text_xMin_projeter.text())
      kwargs["pointMin_y"] = float(self.text_yMin_projeter.text())
      kwargs["pointMax_x"] = float(self.text_xMax_projeter.text())
      kwargs["pointMax_y"] = float(self.text_yMax_projeter.text())
      projeterFichierPoints(kwargs)
    else:
      self.ecritConsole("selectionnez un fichier à filtrer")

  def visualiserPointsIntensite(self):
    fichier = self.text_fichierPoints_visu.text()
    if not fichier:
      self.OnSearch_fichierPoints_visu()

    fichier = self.text_fichierPoints_visu.text()
    if fichier:
      self.visuPoints.proportionPtsAfficher = int(self.text_proportionPoints_visu.text())
      self.visuPoints.afficheFichierPointsExtraits(fichier, typeVisu = "intensite", iMin = float(self.text_iMin_visu.text()), iMax = float(self.text_iMax_visu.text()), dMin = float(self.text_dMin_visu.text()), dMax = float(self.text_dMax_visu.text()), zMin = float(self.text_zMin_visu.text()), zMax = float(self.text_zMax_visu.text()))
    else:
      self.ecritConsole("selectionnez un fichier à visualiser")

  def visualiserPointsDeviation(self):
    fichier = self.text_fichierPoints_visu.text()
    if not fichier:
      self.OnSearch_fichierPoints_visu()

    fichier = self.text_fichierPoints_visu.text()
    if fichier:
      self.visuPoints.proportionPtsAfficher = int(self.text_proportionPoints_visu.text())
      self.visuPoints.afficheFichierPointsExtraits(fichier, typeVisu = "deviation", iMin = self.text_iMin_visu.text(), iMax = self.text_iMax_visu.text(), dMin = self.text_dMin_visu.text(), dMax = self.text_dMax_visu.text(), zMin = self.text_zMin_visu.text(), zMax = self.text_zMax_visu.text())
    else:
      self.ecritConsole("selectionnez un fichier à visualiser")

  def visualiserDistributionDensite(self):
    dir = self.getRepertoireDensite(self.textDossierOutput)
    fichier = QFileDialog.getOpenFileName(self, u"Selectionner le fichiers de densité pour visu de la distribution de densite", dir)
    if fichier:
      if self.maille.nomFichier != unicode(fichier):
        self.maille = getMaillage(unicode(fichier))
      self.maille.afficheDistributionDensite(cheminImage)

  def visualiserProfilLAI(self):
    dir = self.getRepertoireDensite(self.textDossierOutput)
    fichier = QFileDialog.getOpenFileName(self, u"Selectionner le fichiers de densité pour visu du profil de LAI", dir)
    if fichier:
      if self.maille.nomFichier != unicode(fichier):
        self.maille = getMaillage(unicode(fichier))
      self.maille.afficheProfilLAI(cheminImage)


  def visualiserFichierPointsLAS(self):
    dir = self.getRepertoirePoints(self.text_fichierPoints_visu)
    fichier = QFileDialog.getOpenFileName(self, u"Fichier de points LAS à visualiser", dir)
    if fichier:
      self.visuPoints.proportionPtsAfficher = int(self.text_proportionPoints_visu.text())
      self.visuPoints.affichePointsLAS(unicode(fichier))


  def visualiserFichierPointsALS(self):
    dir = self.getRepertoirePoints(self.text_fichierPoints_visu)
    fichier = QFileDialog.getOpenFileName(self, u"Fichier de points ALS à visualiser", dir)
    if fichier:
      self.visuPoints.proportionPtsAfficher = int(self.text_proportionPoints_visu.text())
      self.visuPoints.visualiserFichierPointsALS(fichier)


def main():

    app = QApplication(sys.argv)
    app.setStyle(QStyleFactory.create("plastique"))
    app.setWindowIcon(QIcon('logob.ico'))
    ihm = IHMVoxelsMain()
    sys.exit(app.exec_())


if __name__ == '__main__':
    main()
