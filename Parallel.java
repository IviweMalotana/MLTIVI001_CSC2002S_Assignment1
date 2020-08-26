import java.util.*;
import java.lang.*;
import java.lang.Math;
import java.io.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ForkJoinPool;
import java.util.List;
import java.io.FileWriter;

public class Parallel extends RecursiveAction{

  private String[][] arr;
  private int x_dimension;
  private int y_dimension;
  private int x;
  private int y;
  private double v;
  private double min;
  private double N;
  private double S;
  private double E;
  private double W;
  private double NE;
  private double SE;
  private double NW;
  private double SW;
	private int midx;
  private int midy;
  private int i;
  private int j;
  private int count;
  private static final int threshold = 1000000;
  private String outfile;
  private String result;
  private List<String> mylist;

  public Parallel(List<String> mylist,String outfile,String[][] arr,int i,int j, int x_dimension, int y_dimension){
    this.mylist = mylist;
    this.outfile = outfile;
    this.arr = arr;
    this.i = i;
    this.j = j;
    this.x_dimension = x_dimension;
    this.y_dimension = y_dimension;
  }

  protected void compute(){
    if ((this.x_dimension-this.i)<threshold){
      if ((this.y_dimension-this.j)<threshold){
        FindMin(this.i,this.j,this.x_dimension,this.y_dimension);
      }
      else{
        midy = (this.y_dimension+j)/2;
        invokeAll(new Parallel(this.mylist,this.outfile,this.arr,this.i,this.j,this.x_dimension,midy),new Parallel(this.mylist,this.outfile,this.arr,this.i,midy,this.x_dimension,this.y_dimension));
      }
    }
    else{
			midx = (this.x_dimension+i)/2;
			invokeAll(new Parallel(this.mylist,this.outfile,this.arr,this.i,this.j,midx,this.y_dimension),new Parallel(this.mylist,this.outfile,this.arr,midx,this.j,this.x_dimension,this.y_dimension));
		}
  }

  synchronized void FindMin(int iq, int jq, int x_dimensionq, int y_dimensionq){
		int newi = iq;
		int xq = x_dimensionq;
		int yq = y_dimensionq;

    while(newi<xq){
      for(int newj=jq;newj<yq;newj++){
          v(newi,newj);
          E(newi,newj);
          N(newi,newj);
          NE(newi,newj);
          S(newi,newj);
          SE(newi,newj);
          NW(newi,newj);
          W(newi,newj);
          SW(newi,newj);
          min = Math.min(v,Math.min(E,Math.min(N,Math.min(NE,Math.min(S,Math.min(SE,Math.min(NW,Math.min(W,SW))))))));
          if (v==min){
            x = newi;
            y = newj;
            result = x+" "+y;
            mylist.add(result);
          }
      }
			newi++;
    }
  }
  public void v(int ha, int ho){
    v = Double.parseDouble(this.arr[ha][ho])+0.01;
  }
  public void N(int ha, int ho){
    N = Double.parseDouble(this.arr[ha][ho-1]);
  }
  public void S(int ha, int ho){
    S = Double.parseDouble(this.arr[ha][ho+1]);
  }
  public void W(int ha, int ho){
    W = Double.parseDouble(this.arr[ha-1][ho]);
  }
  public void E(int ha, int ho){
    E = Double.parseDouble(this.arr[ha+1][ho]);
  }
  public void NE(int ha, int ho){
    NE = Double.parseDouble(this.arr[ha+1][ho-1]);
  }
  public void NW(int ha, int ho){
    NW = Double.parseDouble(this.arr[ha-1][ho-1]);
  }
  public void SE(int ha, int ho){
    SE = Double.parseDouble(this.arr[ha+1][ho+1]);
  }
  public void SW(int ha, int ho){
    SW = Double.parseDouble(this.arr[ha-1][ho+1]);
  }

  public void result(){
    try{
      BufferedWriter out = new BufferedWriter(new FileWriter(outfile, true));
      out.write(mylist.size()+"\n");
      for (int a=0;a<mylist.size();a++){
        out.write(mylist.get(a)+"\n");
      }
      out.close();
    }
    catch (IOException e){
      e.printStackTrace();
    }
  }
  static long startTime = 0;

	private static void tick(){
		startTime = System.currentTimeMillis();
	}
	private static float tock(){
		return (System.currentTimeMillis() - startTime) / 1000.0f;
	}
  public static void main(String[] args){

    String infile = args[0];
    String outfile = args[1];
    int x_dimension = 0;
    int y_dimension = 0;
    String[][] arr = new String[x_dimension][y_dimension];
    List<String> mylist = new ArrayList<>();

    File file = new File(outfile);

    try(BufferedReader br = new BufferedReader(new FileReader(infile))){
      String firstline = br.readLine();
      String[] dimensions = firstline.split(" ");
      x_dimension = Integer.parseInt(dimensions[0]);
      y_dimension = Integer.parseInt(dimensions[1]);
      arr = new String[x_dimension][y_dimension];
      int linecount=0;
      while (br.readLine()!=null){
        linecount++;
      }
      if (linecount==1){
        try(BufferedReader br1 = new BufferedReader(new FileReader(infile))){
          String firstline1 = br1.readLine();
          String[] line = (br1.readLine()).split(" ");
          int l=0;
          for(int in=0;in<y_dimension;in++){
            for(int jn=0;jn<x_dimension;jn++){
              arr[in][jn] = line[l];
              l++;
            }
          }
        }
        catch(IOException e){
          e.printStackTrace();
        }
      }
      else{
        try(BufferedReader br2 = new BufferedReader(new FileReader(infile))){
          String firstline2 = br2.readLine();
          for(int in=0;in<y_dimension;in++){
            String[] line = (br2.readLine()).split(" ");
            for(int jn=0;jn<x_dimension;jn++){
              arr[in][jn] = line[jn];
            }
          }
        }
        catch(IOException e){
          e.printStackTrace();
        }
      }
    }
    catch(IOException e){
      e.printStackTrace();
    }
    int newx = x_dimension-1;
    int newy = y_dimension-1;
		tick();
    Parallel tr = new Parallel(mylist,outfile,arr,1,1,newx,newy);
    ForkJoinPool fp = ForkJoinPool.commonPool();
    fp.invoke(tr);
		float time = tock();
		System.out.println("Run took "+ time +" seconds");
    tr.result();
		System.gc();
  }
}
