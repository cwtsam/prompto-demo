package com.stfalcon.chatkit.sample.features.main;

import com.stfalcon.chatkit.sample.utils.RelaxInterface;
import com.stfalcon.chatkit.sample.features.main.org.ahlab.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class EmpaticaData {
	private static final int LIMIT = 60; // frequency of checks
	private static EmpaticaData instance;
	private List<Double> ibiList;
	private List<Double> rmssdList;
	private List<Float> edaList;
	private RelaxInterface relaxInterface;
	private double ibiTime;
	private double edaTime;
	private double accTime;
	private double bvpTime;

	private int y = 0;
	private int count = 0;
	private double rmssdBase = 60; // enter average baseline RMSSD value
	private float edaBase = (float) 0.55; // enter average baseline EDA value
	private double rmssdAvg = 0;
	private float edaAvg = 0;

	private EmpaticaData() {
		ibiList = new ArrayList<>();
		rmssdList = new ArrayList<>();
		edaList = new ArrayList<>();
		// starts the file writing, creates new folder
		//stores in internal storage of phone, Documents (if not present, you would have to create folders)
		// enable storage permission for app as well
		FileWriter  fileWriter = FileWriter.getInstance();
		fileWriter.initSession("Eval_pilots","24",2,1); /// change folder and info here
	}

	public void TimeTagBegin(){
		FileWriter.getInstance().appendTime(edaTime+","+ibiTime+","+accTime+","+"begin");
		FileWriter.getInstance().appendEDA(" "+","+" "+","+"begin");
		FileWriter.getInstance().appendIBI(" "+","+" "+","+" "+","+" "+","+" "+","+" "+","+"begin");
		FileWriter.getInstance().appendAcc(" "+","+" "+","+" "+","+" "+"begin");
		System.out.println("BEGIN: EDA Timestamp=" + edaTime + " IBI Timestamp=" + ibiTime);
	}

	public void TimeTagEnd(){
		//new Date().getTime();
		FileWriter.getInstance().appendTime(edaTime+","+ibiTime+","+accTime+","+"end");
		FileWriter.getInstance().appendEDA(" "+","+" "+","+"end");
		FileWriter.getInstance().appendIBI(" "+","+" "+","+" "+","+" "+","+" "+","+" "+","+"end");
		FileWriter.getInstance().appendAcc(" "+","+" "+","+" "+","+" "+"end");
		System.out.println("END: EDA Timestamp=" + edaTime + " IBI Timestamp=" + ibiTime);
	}

	public static synchronized EmpaticaData getInstance() {
		if (instance == null) {
			instance = new EmpaticaData();
		}
		return instance;
	}

	public void setRelaxInterface(RelaxInterface relaxInterface) {
		this.relaxInterface = relaxInterface; //this is to help call the "relax" event
	}

	void pushIBI(float ibi, double ibitime) {

		double ms = 1000 * ibi;
		double HR = 60/ibi;
		double rmssd = 0;
		double sdnn = 0;
		double ratio = 0;

		ibiTime = ibitime;
		ibiList.add(ms);

		if (ibiList.size() > 61){
			// calculate RMSSD
			double x = 0;
			for (int i = y; i < ibiList.size()-1; i++) {
				x += Math.pow((ibiList.get(i) - ibiList.get(i+1)), 2);
			}
			rmssd = Math.sqrt(x/(ibiList.size()-1-y));

			// calculate SDNN
			double z = 0;
			double ibiSum = 0;
			for (int i = y; i < ibiList.size()-1; i++) {
				ibiSum += ibiList.get(i);
			}
			double ibiAvg = ibiSum/(ibiList.size()-1-y);

			for (int i = y; i < ibiList.size()-1; i++) {
				z += Math.pow((ibiList.get(i) - ibiAvg), 2);
			}

			sdnn = Math.sqrt(z/(ibiList.size()-1-y));

			y++;
			// calculate ratio
			ratio = sdnn/rmssd;
			rmssdList.add(rmssd);
			count++;
		}

		// log data to file
		FileWriter.getInstance().appendIBI(ibitime+","+ibi+","+HR+","+rmssd+","+sdnn+","+ratio); /// append by separating with commas

		if (count > LIMIT) {
			double rmssdSum = 0;
			for (int i = 0; i < rmssdList.size()-1; i++) {
				rmssdSum += rmssdList.get(i);
			}
			rmssdAvg = rmssdSum/(rmssdList.size()-1);
			updateStress();
			rmssdList.clear();
			count = 0;
		}

	}

	void pushEDA(float eda, double edatime) {
		FileWriter.getInstance().appendEDA(edatime+","+eda);
		edaTime = edatime;

		edaList.add(eda);

		if (edaList.size() > 4) {
			float edaSum = 0;
			for (int i = 0; i < edaList.size()-1; i++) {
				edaSum += edaList.get(i);
			}
			edaAvg = edaSum/(edaList.size()-1);
		}

		if (count > LIMIT) {
			edaList.clear();
		}

	}


	void pushACC(int x, int y, int z, double acctime) {
		FileWriter.getInstance().appendAcc(acctime+","+x+","+y+","+z);
		accTime = acctime;
	}

	void pushBVP(float bvp, double bvptime) {
		FileWriter.getInstance().appendBVP(bvptime+","+bvp);
		bvpTime = bvptime;
	}


	private void updateStress() {
		float edaLower = ((float)0.6)*edaBase;
		float edaUpper = ((float)1.4)*edaBase;
		double rmssdLower = ((float)0.8)*rmssdBase;

		if (edaAvg > edaLower && edaAvg < edaUpper && rmssdAvg > rmssdLower) {
			relaxInterface.onRelax();
			System.out.println("relaxed");
		}
	}

}
