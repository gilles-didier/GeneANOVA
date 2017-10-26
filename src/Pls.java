import java.io.*;
class Pls {
    //***********************************************************************************************
        public static PlsMat[][] nipals ( PlsMat mat) throws java.io.IOException {
	        if (mat.NaNinside()) throw new java.io.IOException("Pas de NaN avec Nipals");
                double preci = 1E-6;
                int nl = mat.getRowDimension();
                int nc = mat.getColumnDimension();
		int rang;
		if (Math.min(nc,nl) == nl) rang = nl-1;
		else rang = nc;   //calcul du rang (-1 cause centrage) 
                PlsMat[][] X = new PlsMat[rang][2];
                PlsMat[] ph = new PlsMat[rang];// construction Vecteurs ph
                PlsMat[] th = new PlsMat[rang];// construction Vecteurs th
                PlsMat xh0 = new PlsMat(nl,nc);//construction Matrices temporaires
                PlsMat xh1 = new PlsMat(nl,nc);//construction Matrices temporaires
	        PlsMat pha = new PlsMat(nl,1);//Matrice utilitaire de convergence de ph
		for(int i=0; i< rang; i++) {
		    ph[i] = new PlsMat(nc,1);
		    th[i] = new PlsMat(nl,1);
		 }//Instantation de vecteurs matrices ph et th   
		try{
			xh0 = mat.center();// Initialisation de X_0
			xh0 = xh0.reduce();//on reduit
		} catch (IOException io) {}//test si pas de colonne de nan 
		
                for (int h = 0; h < rang; h++)// boucle generale 
                {
	          th[h] = xh0.getupperMatrix(nl,1);//th = premiere colonne de Xh-1
		  ph[h] = (xh0.transpose().times(th[h])).times(1/th[h].normE());//ph='Xh-1*th
		  ph[h].normaliser();//normer ph a 1
		  th[h] = xh0.times(ph[h]);//th=xh-1*ph/phph (phph=1)
		    do {
		     	pha = ph[h];
		     	ph[h] = (xh0.transpose().times(th[h])).times(1/th[h].normE());//ph='Xh-1*th
		     	ph[h].normaliser();//normer ph a 1
		     	th[h] = xh0.times(ph[h]);//th=xh-1*ph/phph (phph=1) 
		        } while(pha.minus(ph[h]).normE() > preci);
		   xh1 = xh0.minus(th[h].times(ph[h].transpose()));
		   xh0 = xh1;//on a introduit xh1 pour calcul sur de xh+1=f(xh)
                 }
		for(int h = 0; h < rang; h++) {
		      X[h][0] = ph[h];
		      X[h][1] = th[h];
                } 
             return X;
        }

//********NIPALS****DONNEES******Manquantes*************************************************************

 public static PlsMat[][] nipals2( PlsMat mat) throws java.io.IOException {
                double preci = 1E-6;
                int nl = mat.getRowDimension();
                int nc = mat.getColumnDimension();
		int rang;
		if (Math.min(nc,nl) == nl) rang = nl-1;
		else rang = nc;                       //calcul du rang (-1 cause centrage)int rang;
		PlsMat[][] X = new PlsMat[rang][2];
                PlsMat[] ph = new PlsMat[rang];// construction Vecteurs ph
                PlsMat[] th = new PlsMat[rang];// construction Vecteurs th
                PlsMat xh0 = new PlsMat(nl,nc);//construction Matrices temporaires
                PlsMat xh1 = new PlsMat(nl,nc);//construction Matrices temporaires
	        PlsMat pha = new PlsMat(nl,1);//Matrice utilitaire de convergence de ph
		for(int i=0; i< rang ; i++) {
		    ph[i] = new PlsMat(nc,1);
		    th[i] = new PlsMat(nl,1);
		}//Instantation de vecteurs matrices ph et th
		try{ 
			xh0 = mat.center();// centrer et initialiser Xo
			xh0 = xh0.reduce();//on reduit 
		} catch (IOException ioe) {throw ioe/* new java.io.IOException("column of 0 or 1 NaN")*/;}
		
                for (int h = 0; h < rang; h++)// boucle generale 
                {
	          th[h] = xh0.getupperMatrix(nl,1);//th = premiere colonne de Xh-1
		  ph[h] = xh0.regress1(th[h]);
		  ph[h].normaliser();//normer ph a 1
		  th[h] = xh0.regress2(ph[h]);
		    do {
		     	pha = ph[h];
		     	ph[h] = xh0.regress1(th[h]);// regression de Xh sur ph
		     	ph[h].normaliser();//normer ph a un
		     	th[h] = xh0.regress2(ph[h]);// regression de Xh sur ph
		        } while(pha.minus(ph[h]).normE() > preci);
		   xh1 = xh0.minus(th[h].times(ph[h].transpose()));
		   xh0 = xh1;//on a introduit xh1 pour calcul sur de xh+1=f(xh)
                 }
		for(int h = 0; h < rang; h++) {
		      X[h][0] = ph[h];
		      X[h][1] = th[h];
                } 
             return X;
        }


//*******************PLS2**classique*********ET**PLS**Modifie**********************************************************************

//// PLS2 modifié = petite modf ou seul les u[h] changent et ou u[h] devient combinaison linéaire des Y

// si Donnéees manquantes les valeurs de t[h] c[h] et sont differentes de PLS2 classique

// Le dernier argument de la methode choisi la version true = classique, false = modifié

public static PlsMat[][] pls2( PlsMat mat, int ncx, int ncy, boolean version1) throws java.io.IOException {
	double preci = 1E-8;
	int nl = mat.getRowDimension();
        int nc = mat.getColumnDimension();
	PlsMat X0 = new PlsMat(nl,ncx);//construction Matrices temporaires
	PlsMat X1 = new PlsMat(nl,ncx);//construction Matrices temporaires 
	PlsMat Y0 = new PlsMat(nl,ncy);//construction Matrices temporaires
	PlsMat Y1 = new PlsMat(nl,ncy);//construction Matrices temporaires
	PlsMat Y = new PlsMat(nl,ncy);//construction Matrices temporaires
	int rangx ;
	if (Math.min(nl,ncx) == nl) 
		rangx = nl-1;//calcul du rang (-1 cause centrage)int rang;
	else 
		rangx = ncx;   
	PlsMat[][] M = new PlsMat[rangx][5];	
                 
	PlsMat[] p = new PlsMat[rangx];// construction Vecteurs ph
        PlsMat[] t = new PlsMat[rangx];// construction Vecteurs th
	PlsMat[] c = new PlsMat[rangx];// construction Vecteurs th
	PlsMat[] u = new PlsMat[rangx];// construction Vecteurs th
        PlsMat[] w0 = new PlsMat[rangx];//Matrice utilitaire de convergence de ph
        PlsMat w1 = new PlsMat(nl,1);//Matrice utilitaire de convergence de ph
	 
	try{ 
		mat = mat.center();// centrer et initialiser Xo
		mat = mat.reduce();//on reduit 
	} catch (IOException ioe) {throw ioe/* new java.io.IOException("column of 0 or 1 NaN")*/;}	
	X0 = mat.getMatrix(0,nl-1,0,ncx-1);
	Y0 = mat.getMatrix(0,nl-1,ncx,ncx+ncy-1);
	Y = Y0;
	
	for (int h = 0; h < rangx; h++){  // boucle generale 
		if (version1)
			u[h] = Y0.getMatrix(0,nl-1,0,0);//uh = premiere colonne de Yh-1 (version classique)
		else 
			u[h] = Y.getMatrix(0,nl-1,0,0);//uh = premiere colonne de Y (version modifié)
		//premiere iteration
		w0[h] = X0.regress1(u[h]);// initialisation
		w0[h].normaliser();
                t[h] = X0.regress2(w0[h]);
		c[h] = Y0.regress1(t[h]);
                if (version1)
			u[h] = Y0.regress2(c[h]);// (version modifié)
		else u[h] = Y.regress2(c[h]);
                //-----------------------------------------
		do {
			w1 = w0[h];
			w0[h] = X0.regress1(u[h]);
			w0[h].normaliser();
			t[h] = X0.regress2(w0[h]);
			c[h] = Y0.regress1(t[h]);
			if (version1)
				u[h] = Y0.regress2(c[h]);// (version modifié)
			else 
				u[h] = Y.regress2(c[h]);
		} while (w1.minus(w0[h]).normE() > preci);
                //------------------------------------------------
		p[h] = X0.regress1(t[h]);
		X1 = X0.minus(t[h].times(p[h].transpose())); 
		Y1 = Y0.minus(t[h].times(c[h].transpose()));
		X0=X1; Y0=Y1;
	}	
	for(int h = 0; h < rangx; h++) {
		      M[h][0] = p[h]; M[h][2] = c[h]; //stockage
		      M[h][1] = t[h]; M[h][3] = u[h];
		      M[h][4] = w0[h];
                } 
        return M;
    }
   
  //*****PLS****DE*********WOLD************************************
  
  // Contient Analyse canonique, analyse des redondances Regression pls classique Analyse canonique PLS
  
  //  version canon ,,  canonPLS,, redon,, PLS 
  
public static PlsMat[][] plsWold( PlsMat mat, int ncx, int ncy, String version, double precision) throws java.io.IOException { 
        double preci = precision;
	boolean modA_ah; boolean modA_bh; boolean canonique; boolean redu_th;
	boolean redu_uh; boolean invariant; boolean regression;
	int nl = mat.getRowDimension();
        int nc = mat.getColumnDimension();
	PlsMat X0 = new PlsMat(nl,ncx);//construction Matrices temporaires
	PlsMat X1 = new PlsMat(nl,ncx);//construction Matrices temporaires 
	PlsMat Y0 = new PlsMat(nl,ncy);//construction Matrices temporaires
	PlsMat Y1 = new PlsMat(nl,ncy);//construction Matrices temporaires
	PlsMat Y = new PlsMat(nl,ncy);//construction Matrices temporaires
        
	int rangx;  // a voir le calcul du rang en construction
	if (Math.min(nl,ncx) == nl) rangx = nl-1;
		else rangx = ncx;                    //calcul du rang (-1 cause centrage)int rang;
        int rangy;  // a voir le calcul du rang en construction
	
	if (Math.min(nl,ncy) == nl) rangy = nl-1;
		else rangy = ncy; 
	int rang = Math.min(rangx,rangy);
	PlsMat[][] M = new PlsMat[rang][7];	                       
	PlsMat[] c = new PlsMat[rang];// declaration Vecteurs ch
        PlsMat[] t = new PlsMat[rang];//  declaration Vecteurs th
	PlsMat[] d = new PlsMat[rang];//  declaration Vecteurs dh
	PlsMat[] u = new PlsMat[rang]; //  declaration Vecteurs uh
	PlsMat[] e = new PlsMat[rang]; //  declaration Vecteurs eh
	PlsMat[] b = new PlsMat[rang]; //  declaration Vecteurs eh
        PlsMat[] a0 = new PlsMat[rang]; // 
        PlsMat a1 = new PlsMat(nl,1); //Matrice utilitaire de convergence de ahPlsMat a1 = new PlsMat(nl,1); //Matrice utilitaire de convergence de ah
	PlsMat a2 = new PlsMat(nl,1);
        //---------------------------------------------------
	if (version == "canon") {
	modA_ah = false; redu_th =true; modA_bh =false; canonique = true; 
	redu_uh = true;	regression = false; invariant = false;
	}
	else{ 
        	if (version == "redon") {
		modA_ah = false; redu_th =true; modA_bh = true; canonique = false; 
		redu_uh = true; regression = false; invariant = true;// uh sans consequences
        	}
		else{
        		if (version == "canonPls") {
			modA_ah = true; redu_th = true; modA_bh = true; canonique = true; 
			redu_uh = true; regression = false; invariant = false;
        		}
			else{
	 			if (version == "Pls") {
	 			modA_ah = true; redu_th = false; modA_bh = true ; canonique = false; 
				redu_uh = false; regression = true; invariant = false;}
				else { throw new java.io.IOException("Mauvais arguments dans la methode plswold");}
        }}}
        
	//----------------------------------- 
	try{ 
		mat = mat.center();// centrer et initialiser Xo
		mat = mat.reduce();//on reduit 
		} catch (IOException ioe) {throw ioe/* new java.io.IOException("column of 0 or 1 NaN")*/;}
			
	X0 = mat.getMatrix(0,nl-1,0,ncx-1);
	Y0 = mat.getMatrix(0,nl-1,ncx,ncx+ncy-1);
	Y = Y0;
	
	for (int h = 0; h < rang; h++){  // boucle generale 
	
		t[h] = X0.getMatrix(0,nl-1,0,0); // th = premiere colonne de Xh-1 
		u[h] = Y0.getMatrix(0,nl-1,0,0);//uh = premiere colonne de Yh-1  
		//----------initialisation--------------------------- 
	        if (modA_ah) 
			a0[h] = X0.regress1(u[h]);
		else   
		        {
			try {a2 = (X0.transpose().times(X0)).inv2();
			a0[h] = a2.times(X0.transpose().times(u[h]));
			}catch (Exception ii) {a0[h]=a1;}
			}
		a0[h].normaliser();
		t[h] = X0.regress2(a0[h]);
		if (redu_th) 
			t[h] = t[h].reduce();   // selon version
		if (modA_bh)
			 b[h] = Y0.regress1(t[h]);
		else {
			try{a2 = (Y0.transpose().times(Y0)).inv2();
			b[h] = a2.times(Y0.transpose().times(t[h]));
			}catch (Exception ii){};
			}
		if (version != "Pls") 
			b[h].normaliser();
			
		u[h] = Y0.regress2(b[h]);
		
		if (redu_uh) 
			u[h] = u[h].reduce();   // selon version
         
		//-------------------------------------
		do {
			a1 = a0[h];
			a1.print(5,10);
			if (modA_ah) 
				a0[h] = X0.regress1(u[h]);
			else  {
			        try{
				a2 = (((X0.transpose()).times(X0)).inv2());
				a0[h] = a2.times(X0.transpose().times(u[h]));
				}catch (Exception ii) {a0[h]=a1;}
				}
				
			a0[h].normaliser();
			t[h] = X0.regress2(a0[h]);
			if (redu_th)
				t[h] = t[h].reduce();   // selon version
			if (modA_bh)
				b[h] = Y0.regress1(t[h]);
			else    
			        {
			        try {
				b[h] = (Y0.transpose().times(Y0)).inv2().times(Y0.transpose().times(t[h]));
				}catch (Exception ii) {a0[h]=a1;}
				}
			if (version != "Pls") 
				b[h].normaliser();
			u[h] = Y0.regress2(b[h]);
			if (redu_uh) 
				u[h] = u[h].reduce();   // selon version
			//System.out.println("reste = "+a1.minus(a0[h]).normE());
		} while (a1.minus(a0[h]).normE() > preci);
		//------------------------------------------------
		
		c[h] = X0.regress1(t[h]);
		d[h] = Y0.regress1(t[h]);
		e[h] = Y0.regress1(u[h]);
		
		
		X1 = X0.minus(t[h].times(c[h].transpose())); 
		X0=X1;
		
		if (invariant) {Y1 = Y; //les 3 cas s exclus mutuellement
		System.out.println("invariant");
		}
		if (regression) {Y1 = Y0.minus(t[h].times(d[h].transpose()));
		System.out.println("regression");
		}
		if (canonique)  {Y1 = Y0.minus(u[h].times(e[h].transpose()));
		System.out.println("canonique");
		}
		//System.out.println("aa");
		Y0 = Y1; 
	}	
	for(int h = 0; h < rang; h++) {
	              M[h][0] = a0[h]; M[h][1] = b[h];
		      M[h][2] = t[h];  M[h][3] = u[h];
		      M[h][4] = c[h];  M[h][5] = d[h]; //stockage
		      M[h][6] = e[h];
                } 
        return M;
 
   }
  
}
