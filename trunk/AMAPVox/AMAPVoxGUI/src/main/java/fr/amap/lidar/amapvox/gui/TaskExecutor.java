package fr.amap.lidar.amapvox.gui;

///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package fr.amap.amapvox.gui;
//
//import fr.amap.lidar.amapvox.commons.configuration.Configuration;
//import fr.amap.amapvox.commons.util.TimeCounter;
//import fr.amap.amapvox.simulation.transmittance.TransmittanceSim;
//import fr.amap.amapvox.simulation.transmittance.VirtualMeasuresCfg;
//import fr.amap.amapvox.voxelisation.ProcessTool;
//import fr.amap.amapvox.voxelisation.ProcessToolListener;
//import fr.amap.amapvox.voxelisation.configuration.ALSVoxCfg;
//import fr.amap.amapvox.voxelisation.configuration.MultiResCfg;
//import fr.amap.amapvox.voxelisation.configuration.MultiVoxCfg;
//import fr.amap.amapvox.voxelisation.configuration.TLSVoxCfg;
//import fr.amap.amapvox.voxelisation.configuration.VoxMergingCfg;
//import fr.amap.amapvox.voxelisation.multires.ProcessingMultiRes;
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ArrayBlockingQueue;
//import java.util.concurrent.BlockingQueue;
//import javafx.application.Platform;
//import javafx.beans.value.ChangeListener;
//import javafx.beans.value.ObservableValue;
//import javafx.concurrent.Service;
//import javafx.concurrent.Task;
//import javafx.concurrent.Worker;
//import javafx.concurrent.WorkerStateEvent;
//import javafx.event.ActionEvent;
//import javafx.event.EventHandler;
//import javafx.scene.control.Alert;
//import javafx.scene.control.Button;
//import javafx.stage.Modality;
//import org.apache.log4j.Logger;
//import org.controlsfx.dialog.ProgressDialog;
//import org.jdom2.JDOMException;
//
///**
// *
// * @author calcul
// */
//public class TaskExecutor {
//    
//    private final static Logger logger = Logger.getLogger(TaskExecutor.class);
//    
//    private BlockingQueue<File> queue = new ArrayBlockingQueue<>(100);
//    
//    public void executeTaskList(List<File> tasks){
//        
//
//        queue = new ArrayBlockingQueue<>(tasks.size());
//        queue.addAll(tasks);
//        int taskNumber = tasks.size();
//        int taskID = 1;
//
//        try {
//            if (!queue.isEmpty()) {
//                executeProcess(queue.take());
//            }
//
//        } catch (InterruptedException ex) {
//            logger.error("Process interrupted", ex);
//        }
//    }
//    
//    private void executeProcess(final File file) {
//
//        try {
//            final String type = Configuration.readType(file);
//            
//            ProgressDialog d;
//            final Service<Void> service;
//            
//            final ProcessTool voxTool = new ProcessTool();
//            voxTool.setCoresNumber((int) sliderRSPCoresToUse.getValue());
//            
//            final long start_time = System.currentTimeMillis();
//                    
//
//            service = new Service<Void>() {
//
//                @Override
//                protected Task<Void> createTask() {
//                    return new Task<Void>() {
//                        @Override
//                        protected Void call() throws InterruptedException {
//
//                            final String msgTask = "Task " + taskID + "/" + taskNumber + " :" + file.getAbsolutePath();
//                            updateMessage(msgTask);
//
//                            switch (type) {
//                                
//                                case "transmittance":  
//                                case "LAI2000":
//                                case "LAI2200":
//                                    
//                                    VirtualMeasuresCfg cfg;
//                                    try {
//                                        cfg = VirtualMeasuresCfg.readCfg(file);
//                                        try {
//                                            TransmittanceSim.simulationProcess(cfg);
//                                        }catch(IOException ex){
//                                            logger.error(ex.getMessage());
//                                            showErrorDialog(ex);
//                                        }
//                                        
//                                    } catch (IOException | JDOMException ex) {
//                                        logger.error("Cannot read configuration file", ex);
//                                        showErrorDialog(ex);
//                                    }
//                                    
//                                    
//                                break;
//
//                                case "merging":
//                                    
//                                    final VoxMergingCfg voxMergingCfg = new VoxMergingCfg();
//                                    
//                                    try {
//                                        voxMergingCfg.readConfiguration(file);
//                                        
//                                        voxTool.mergeVoxelFiles(voxMergingCfg/*voxMergingCfg.getFiles(), voxMergingCfg.getOutputFile(), 0, voxMergingCfg.getVoxelParameters().getMaxPAD()*/);
//
//                                        Platform.runLater(new Runnable() {
//
//                                            @Override
//                                            public void run() {
//                                                addFileToVoxelList(voxMergingCfg.getOutputFile());
//                                                setOnSucceeded(null);
//                                            }
//                                        });
//
//                                    } catch (JDOMException ex) {
//                                        logger.error("Cannot parse configuration file");
//                                    } catch (IOException ex) {
//                                        logger.error("Cannot read configuration file");
//                                    }
//                                    
//                                    break;
//
//                                case "voxelisation-ALS":
//
//                                    
//                                    try {
//                                        
//                                        final ALSVoxCfg aLSVoxCfg = new ALSVoxCfg();
//                                        aLSVoxCfg.readConfiguration(file);
//                                        
//                                        voxTool.addProcessToolListener(new ProcessToolListener() {
//
//                                            @Override
//                                            public void processProgress(String progress, int ratio) {
//                                                Platform.runLater(new Runnable() {
//
//                                                    @Override
//                                                    public void run() {
//
//                                                        updateMessage(msgTask + "\n" + progress);
//                                                    }
//                                                });
//                                            }
//
//                                            @Override
//                                            public void processFinished(float duration) {
//
//                                                logger.info("las voxelisation finished in " + TimeCounter.getElapsedStringTimeInSeconds(start_time));
//                                            }
//                                        });
//
//                                        try{
//                                            voxTool.voxeliseFromAls(aLSVoxCfg);
//
//                                            Platform.runLater(new Runnable() {
//
//                                                @Override
//                                                public void run() {
//
//                                                    addFileToVoxelList(aLSVoxCfg.getOutputFile());
//                                                }
//                                            });
//                                            
//                                        }catch(IOException ex){
//                                            logger.error(ex.getMessage(), ex);
//                                            showErrorDialog(ex);
//                                        }catch(Exception ex){
//                                            logger.error(ex.getMessage(), ex);
//                                            showErrorDialog(ex);
//                                        }
//                                        
//                                        
//                                    } catch (Exception ex) {
//                                        logger.error(ex.getLocalizedMessage());
//                                    }
//
//                                    break;
//
//                                case "voxelisation-TLS":
//
//                                    final TLSVoxCfg tLSVoxCfg = new TLSVoxCfg();
//                                    try {
//                                        tLSVoxCfg.readConfiguration(file);
//                                        
//                                        voxTool.addProcessToolListener(new ProcessToolListener() {
//
//                                            @Override
//                                            public void processProgress(String progress, int ratio) {
//                                                Platform.runLater(new Runnable() {
//
//                                                    @Override
//                                                    public void run() {
//
//                                                        updateMessage(msgTask + "\n" + progress);
//                                                    }
//                                                });
//                                            }
//
//                                            @Override
//                                            public void processFinished(float duration) {
//
//                                                logger.info("Voxelisation finished in " + TimeCounter.getElapsedStringTimeInSeconds(start_time));
//                                            }
//                                        });
//
//                                        switch (tLSVoxCfg.getInputType()) {
//
//                                            case RSP_PROJECT:
//
//                                                try {
//                                                    final ArrayList<File> outputFiles = voxTool.voxeliseFromRsp(tLSVoxCfg);
//
//                                                    if (tLSVoxCfg.getVoxelParameters().isMergingAfter()) {
//
//                                                        voxTool.addProcessToolListener(new ProcessToolListener() {
//
//                                                            @Override
//                                                            public void processProgress(String progress, int ratio) {
//                                                                Platform.runLater(new Runnable() {
//
//                                                                    @Override
//                                                                    public void run() {
//
//                                                                        updateMessage(msgTask + "\n" + progress);
//                                                                    }
//                                                                });
//                                                            }
//
//                                                            @Override
//                                                            public void processFinished(float duration) {
//
//                                                                logger.info("Voxelisation finished in " + TimeCounter.getElapsedStringTimeInSeconds(start_time));
//                                                            }
//                                                        });
//
//                                                        VoxMergingCfg mergingCfg = new VoxMergingCfg(tLSVoxCfg.getVoxelParameters().getMergedFile(), tLSVoxCfg.getVoxelParameters(), outputFiles);
//
//                                                        //if(!voxTool.isCancelled()){
//                                                        voxTool.mergeVoxelFiles(mergingCfg/*outputFiles, tLSVoxCfg.getVoxelParameters().getMergedFile(), tLSVoxCfg.getVoxelParameters().getTransmittanceMode(), tLSVoxCfg.getVoxelParameters().getMaxPAD()*/);
//                                                        //}
//
//                                                    }
//
//                                                    Platform.runLater(new Runnable() {
//
//                                                        @Override
//                                                        public void run() {
//
//                                                            if (!voxTool.isCancelled()) {
//                                                                for (File file : outputFiles) {
//                                                                    addFileToVoxelList(file);
//                                                                }
//                                                                if (tLSVoxCfg.getVoxelParameters().isMergingAfter()) {
//                                                                    addFileToVoxelList(tLSVoxCfg.getVoxelParameters().getMergedFile());
//                                                                }
//                                                            }
//                                                        }
//                                                    });
//
//                                                } catch (Exception e) {
//
//                                                }
//
//                                                break;
//
//                                            case RXP_SCAN:
//
//                                                voxTool.voxeliseFromRxp(tLSVoxCfg);
//
//                                                Platform.runLater(new Runnable() {
//
//                                                    @Override
//                                                    public void run() {
//
//                                                        addFileToVoxelList(tLSVoxCfg.getOutputFile());
//                                                    }
//                                                });
//
//                                                break;
//                                        }
//                                    } catch (Exception ex) {
//                                        logger.error("Cannot load configuration file", ex);
//                                    }
//
//                                    break;
//
//                                case "multi-resolutions":
//
//                                    final MultiResCfg multiResCfg = new MultiResCfg();
//                                    
//                                    try {
//                                        multiResCfg.readConfiguration(file);
//                                        
//                                        ProcessingMultiRes process = new ProcessingMultiRes(multiResCfg.getMultiResPadMax(), multiResCfg.isMultiResUseDefaultMaxPad());
//
//                                        process.process(multiResCfg.getFiles());
//                                        process.write(multiResCfg.getOutputFile());
//
//                                        Platform.runLater(new Runnable() {
//
//                                            @Override
//                                            public void run() {
//
//                                                addFileToVoxelList(multiResCfg.getOutputFile());
//                                            }
//                                        });
//                                    
//                                    } catch (JDOMException ex) {
//                                        logger.error("Cannot parse configuration file", ex);
//                                    } catch (IOException ex) {
//                                        logger.error("Cannot read configuration file", ex);
//                                    }
//
//                                    
//
//                                    break;
//
//                                case "multi-voxelisation":
//
//                                    MultiVoxCfg multiVoxCfg = new MultiVoxCfg();
//                                    try {
//                                        multiVoxCfg.readConfiguration(file);
//                                        
//                                        voxTool.addProcessToolListener(new ProcessToolListener() {
//
//                                            @Override
//                                            public void processProgress(String progress, int ratio) {
//                                                Platform.runLater(new Runnable() {
//
//                                                    @Override
//                                                    public void run() {
//
//                                                        updateMessage(msgTask + "\n" + progress);
//                                                    }
//                                                });
//
//                                            }
//
//                                            @Override
//                                            public void processFinished(float duration) {
//
//                                                logger.info("las voxelisation finished in " + TimeCounter.getElapsedStringTimeInSeconds(start_time));
//                                            }
//                                        });
//                                        voxTool.multiVoxelisation(multiVoxCfg);
//
//                                    } catch (Exception ex) {
//                                        logger.error("Cannot load configuration file");
//                                    }
//                                    
//                                    break;
//                            }
//
//                            return null;
//                        }
//                    };
//                }
//            };
//            
//            d = new ProgressDialog(service);
//            d.initModality(Modality.NONE);
//            d.initOwner(stage);
//            d.setResizable(true);
//            d.show();
//            Button buttonCancel = new Button("cancel");
//            d.setGraphic(buttonCancel);
//            
//            buttonCancel.setOnAction(new EventHandler<ActionEvent>() {
//
//                @Override
//                public void handle(ActionEvent event) {
//                    service.cancel();
//                    voxTool.setCancelled(true);
//                }
//            });
//
//            service.exceptionProperty().addListener(new ChangeListener<Throwable>() {
//
//                @Override
//                public void changed(ObservableValue<? extends Throwable> observable, Throwable oldValue, Throwable newValue) {
//                    System.out.println("test");
//                }
//            });
//
//            service.stateProperty().addListener(new ChangeListener<Worker.State>() {
//
//                @Override
//                public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
//                    if (newValue == Worker.State.SUCCEEDED) {
//                        if (!queue.isEmpty()) {
//                            try {
//                                taskID++;
//                                executeProcess(queue.take());
//
//                            } catch (InterruptedException ex) {
//                                logger.error("Task processing was interrupted", ex);
//                            }
//                        }
//                    }
//                }
//            });
//
//            service.setOnFailed(new EventHandler<WorkerStateEvent>() {
//
//                @Override
//                public void handle(WorkerStateEvent event) {
//                    logger.error("Service failed : ",service.getException());
//                }
//            });
//
//            service.start();
//            
//
//        } catch (JDOMException | IOException e) {
//            
//            logger.error("An error occured", e);
//            
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            alert.setHeaderText("Incorrect file");
//            alert.setContentText("File is corrupted or cannot be read!\n"
//                    + file.getAbsolutePath());
//            alert.show();
//
//            if (!queue.isEmpty()) {
//                try {
//                    taskID++;
//                    executeProcess(queue.take());
//
//                } catch (InterruptedException ex) {
//                    logger.error("Tasks processing was interrupted", ex);
//                }
//            }
//        }        
//
//    }
//}
