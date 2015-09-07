/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxreader;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import javax.vecmath.Point3d;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Voxel implements Serializable {

        /**
         * indice du voxel, position en x
         */
        public int $i;

        /**
         * indice du voxel, position en y
         */
        public int $j;

        /**
         * indice du voxel, position en z
         */
        public int $k;
        
        /**
         * Nombre de fois où un rayon à échantillonné le voxel
         */
        public int nbSampling;
        
        /**
         * Nombre de fois où un rayon à échantillonné le voxel
         */
        //public int nbOutgoing = 0;

        /**
         * Nombre d'échos dans le voxel
         */
        public int nbEchos;

        /**
         * Longueurs cumulées des trajets optiques dans le voxel dans le cas 
         * où il n'y a pas eu d'interceptions dans le voxel
         */
        //public double lgNoInterception = 0;

        /**
         * Longueurs cumulées des trajets optiques dans le voxel
         * Lorqu'il y a interception dans le voxel vaut: distance du point d'entrée au point d'interception
         * Lorsqu'il y n'y a pas d'interception dans le voxel vaut: distance du point d'entré au point de sortie
         */
        //public double lgOutgoing = 0;
        
        public float lgTotal;

        /**
         * PAD beam fraction, calcul du PAD selon la formule:
         * transmittance = (bfEntering - bfIntercepted) / vox.bfEntering;
         * PAD = log(transmittance) / (-0.5 * lMean2);
         * 
         */
        //public double PadBF = 0;

        /**
         * PAD beam section, calcul du PAD selon la formule:
         * transmittance = (bsEntering - bsIntercepted) / vox.bsEntering;
         * PAD = log(transmittance) / (-0.5 * lMean2);
         * 
         */
        

        /**
         * Beam fraction Entering, fraction de faisceau entrante
         * Nombre de fois où un rayon a échantillonné le voxel,
         * contrairement à nbSampling, peut etre pondéré
         */
        //public double bfEntering = 0;

        /**
         * Beam fraction Intercepted, fraction de faisceau interceptée
         * Nombre d'échos dans le voxel, contrairement à nbEchos peut être pondéré
         */
        //public double bfIntercepted = 0;

        /**
         * Beam Section Entering, section de faisceau entrant,
         * est calculé par rapport à la surface selon la formule:
         * tan(laserBeamDivergence / 2) * distance * Math.PI;
         * 
         */
        //public float bsEntering = 0;

        /**
         * Beam Section Intercepted, section de faisceau intercepté 
         */
        //public float bsIntercepted = 0;

        /**
         * Distance du voxel par rapport au sol
         */
        public float ground_distance;

        /**
         *  Position du voxel dans l'espace
         *  Note: un attribut de voxel commençant par underscore _ 
         * signifie que l'attribut ne doit pas être exporté
         */
        public Point3d _position;
        
        public double _sum_li;
        //public double bvOutgoing = 0;
        //public double bvEntering = 0;
        //public double bvIntercepted = 0;

        /**
         * Longueur moyenne du trajet optique dans le voxel
         * En ALS est égal à: pathLength / (nbSampling)
         * En TLS est égal à: lgTraversant / (nbSampling - nbEchos)
         */
        //public double lMeanOutgoing = 0;
        public double lMeanTotal;
        //public double LMean_NoInterception = 0;
        //public double _transBeforeNorm;
        public float transmittance;
        //public double _transmittance_v2;
        public float angleMean;
        public float bvEntering;
        public float bvIntercepted;
        public float PadBVTotal;
        //public float _PadBVTotal_V2;
        //public double _sumSurfaceMultiplyLength;
        
        public float passNumber;
        public float neighboursNumber;
        

        protected static Set<String> fieldsNames;
        protected static Set<Field> _fields;
        
        private static boolean classInit;
        
        

        /**
         *
         * @param i
         * @param j
         * @param k
         */
        public Voxel(int i, int j, int k) {
            
            this.$i = i;
            this.$j = j;
            this.$k = k;
        }
        
        public Voxel(int i, int j, int k, Class c) {
            
            this.$i = i;
            this.$j = j;
            this.$k = k;
            
            if(!classInit){
                _fields = Voxel.getFields(c);
                classInit = true;
            }
        }
        
        static{
            _fields = Voxel.getFields(Voxel.class);
        }
        
        public Voxel(){
            
        }
        
        public void setPosition(Point3d position){
            this._position = new Point3d(position);
        }
        
        public void setDist(float dist){
            this.ground_distance = dist;
        }
        
        public void setFieldValue(Class< ? extends Voxel> c, String fieldName, Object object, Object value) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
            try {
                Field f = c.getField(fieldName);
                Class<?> type = f.getType();
                float v;
                
                switch(type.getName()){
                    case "double":
                        v = (float)value;
                        f.setDouble(object, (double)v);
                        break;
                    case "float":
                        f.setFloat(object, (float)value);
                        break;
                    case "int":
                        v = (float)value;
                        f.setInt(object, (int)v);
                        break;
                }
                
                
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                throw ex;
            }
        }
        
        public static String getHeader(Class c){
            
            Voxel.getFields(c);
            
            String header = "";

            for (String fieldName : fieldsNames) {

                if(!fieldName.startsWith("_")){

                    if(fieldName.startsWith("$")){
                        header += fieldName.substring(1)+" ";
                    }else{
                        header += fieldName+" ";
                    }

                }
            }

            header = header.trim();

            return header;
        }
        
        protected static Set<Field> getFields(Class c){
            
            fieldsNames = new TreeSet<>();
            
            Field[] fields = c.getFields();
            
            Set<Field> fieldsSet = new TreeSet<>(new Comparator<Field>() {

                @Override
                public int compare(Field o1, Field o2) {
                    
                    return o1.getName().compareTo(o2.getName());
                }
            });
            
            for (Field field : fields) {
                field.setAccessible(true);
                fieldsSet.add(field);
                fieldsNames.add(field.getName());
            }
            
            return fieldsSet;
        }

        @Override
        public String toString() {

            String voxelString = "";

            // compare values now
            for (Field _field : _fields) {
                
                String fieldName = _field.getName();
                
                if (!fieldName.startsWith("_")) {
                    
                    try {
                        Object newObj = _field.get(this);
                        voxelString += newObj + " ";
                    }catch (IllegalArgumentException | IllegalAccessException ex) {
                        //_logger.error(ex);
                    }
                }
            }
            
            voxelString = voxelString.trim();
            return voxelString;
        }
    }
