/**
 * Copyright 2014  XCL-Charts
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 	
 * @Project XCL-Charts 
 * @Description Android图表基类库
 * @author XiongChuanLiang<br/>(xcl_168@aliyun.com)
 * @license http://www.apache.org/licenses/  Apache v2 License
 * @version 1.0
 */
package org.xclcharts.chart;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.xclcharts.renderer.LnChart;
import org.xclcharts.renderer.XEnum;
import org.xclcharts.renderer.line.PlotCustomLine;
import org.xclcharts.renderer.line.PlotDot;
import org.xclcharts.renderer.line.PlotDotRender;
import org.xclcharts.renderer.line.PlotLine;
import org.xclcharts.renderer.plot.PlotKeyRender;

import android.graphics.Canvas;
import android.graphics.Paint.Align;

/**
 * @ClassName LineChart
 * @Description  线图基类
 * @author XiongChuanLiang<br/>(xcl_168@aliyun.com)
 *  
 */
public class LineChart extends LnChart{
	
	//数据源
	protected List<LineData> mDataSet;
	
	//数据轴显示在左边还是右边
	private XEnum.LineDataAxisLocation mDataAxisPosition = XEnum.LineDataAxisLocation.LEFT;

	//用于绘制定制线(分界线)
	private PlotCustomLine mCustomLine = null;
	
	
	public LineChart()
	{
		super();
		initChart();
	}

	private void initChart()
	{		
		mCustomLine = new PlotCustomLine();
		defaultAxisSetting();		
	}
	
	/**
	 * 设置数据轴显示在哪边,默认是左边
	 * @param position 显示位置
	 */
	public void setDataAxisLocation(XEnum.LineDataAxisLocation position)
	{
		mDataAxisPosition = position;				
		defaultAxisSetting();
	}
	
	/**
	 * 依数据库显示位置，设置相关的默认值
	 */
	private void defaultAxisSetting()
	{
		if(XEnum.LineDataAxisLocation.LEFT == mDataAxisPosition)
		{
			categoryAxis.setHorizontalTickAlign(Align.CENTER);
			dataAxis.setHorizontalTickAlign(Align.LEFT);	
		}else{		
			dataAxis.setHorizontalTickAlign(Align.RIGHT);
			dataAxis.getAxisTickLabelPaint().setTextAlign(Align.LEFT);			
		}	
	}
	 
		/**
		 * 分类轴的数据源
		 * @param categories 标签集
		 */
		public void setCategories(List<String> categories)
		{
			categoryAxis.setDataBuilding(categories);
		}
		
		/**
		 *  设置数据轴的数据源
		 * @param dataSet 数据源
		 */
		public void setDataSource(LinkedList<LineData> dataSet)
		{
			this.mDataSet = dataSet;		
		}						
						
		/**
		 * 设置定制线值
		 * @param customLineDataset 定制线数据集合
		 */
		public void setDesireLines(List<CustomLineData> customLineDataset)
		{
			mCustomLine.setCustomLines(customLineDataset);
		}
		
		/**
		 * 绘制线
		 * @param canvas	画布
		 * @param bd		数据类
		 * @param type		处理类型
		 */
		private void renderLine(Canvas canvas, LineData bd,String type)
		{
			float initX =  plotArea.getLeft();
            float initY =  plotArea.getBottom();
             
			float lineStartX = initX;
            float lineStartY = initY;
            float lineEndX = 0.0f;
            float lineEndY = 0.0f;
            						
			float axisScreenHeight = getAxisScreenHeight();
			float axisDataHeight = (float) dataAxis.getAxisRange();		
			
			//得到分类轴数据集
			List<String> dataSet =  categoryAxis.getDataSet();
			if(null == dataSet) return ;
			//步长
			//int XSteps = (int) Math.ceil( getAxisScreenWidth()/ (dataSet.size() - 1)) ;
			float XSteps =  getAxisScreenWidth()/ (dataSet.size() - 1);
			
			List<Double> chartValues = bd.getLinePoint();	
			if(null == chartValues) return ;
			int j = 0;	
						
		    //画线
			for(Double bv : chartValues)
            {																	
				//参数值与最大值的比例  照搬到 y轴高度与矩形高度的比例上来 	                                
            	float valuePostion = (float) Math.round( 
						axisScreenHeight * ( (bv - dataAxis.getAxisMin() ) / axisDataHeight)) ;  
            		                	
            	if(j == 0 )
				{
					lineStartX = initX;
					lineStartY = initY - valuePostion;
					
					lineEndX = lineStartX;
					lineEndY = lineStartY;
				}else{
					lineEndX =  initX + (j) * XSteps;
					lineEndY = initY - valuePostion;
				}            	            	            	           	
            
            	//如果值与最小值相等，即到了轴上，则忽略掉
				if(bv != dataAxis.getAxisMin())
				{
				
	            	PlotLine pLine = bd.getPlotLine();           
	            	if(type.equalsIgnoreCase("LINE"))
	            	{
	            		if( lineStartY != initY )	            			
	            			canvas.drawLine( lineStartX ,lineStartY ,lineEndX ,lineEndY,
	            												pLine.getLinePaint()); 
	            			            			            		
	            	}else if(type.equalsIgnoreCase("DOT2LABEL")){
	            		
	            		if(!pLine.getDotStyle().equals(XEnum.DotStyle.HIDE))
	                	{                		       	
	                		PlotDot pDot = pLine.getPlotDot();	                
	                		float rendEndX  = lineEndX  + pDot.getDotRadius();               		
	            			
	                		PlotDotRender.getInstance().renderDot(canvas,pDot,
	                				lineStartX ,lineStartY ,
	                				lineEndX ,lineEndY,
	                				pLine.getDotPaint()); //标识图形            			                	
	            			lineEndX = rendEndX;
	                	}
	            		
	            		if(bd.getLabelVisible())
	                	{
	                		//fromatter	            			
	                		canvas.drawText(this.getFormatterItemLabel(bv), 
	    							lineEndX, lineEndY,  pLine.getDotLabelPaint());
	                	}
	            	}else{
	            		return ;
	            	}      
				} //if(bv != dataAxis.getAxisMin())
            	
				lineStartX = lineEndX;
				lineStartY = lineEndY;

				j++;
            } 				
			
		}
		
		/**
		 * 绘制图表
		 */
		private void renderVerticalPlot(Canvas canvas)
		{			
								
			if(XEnum.LineDataAxisLocation.LEFT == mDataAxisPosition)
			{
				renderVerticalDataAxis(canvas);
			}else{
				renderVerticalDataAxisRight(canvas);
			}						
			renderVerticalCategoryAxis(canvas);
			if(null == mDataSet) return ;
			
			List<LnData> lstKey = new ArrayList<LnData>();								
			//开始处 X 轴 即分类轴                  
			for(int i=0;i<mDataSet.size();i++)
			{								
				renderLine(canvas,mDataSet.get(i),"LINE");
				renderLine(canvas,mDataSet.get(i),"DOT2LABEL");
				String key = mDataSet.get(i).getLineKey();				
				if("" != key)
					lstKey.add(mDataSet.get(i));
			}			
			
			if(null == plotKey) plotKey = new PlotKeyRender(this);
					plotKey.renderLineKey(canvas, lstKey);
		}	
		 
		
		//绘制图表	
		@Override
		protected boolean postRender(Canvas canvas) throws Exception
		{			
			boolean ret = true;
			try{
				super.postRender(canvas);	
				renderVerticalPlot(canvas);
				
				//画线形图，横向的定制线
				mCustomLine.setVerticalPlot(dataAxis, plotArea, getAxisScreenHeight());
				ret = mCustomLine.renderVerticalCustomlinesDataAxis(canvas);
				
			} catch (Exception e) {
				throw e;
			}
			return ret;
		}
		
		
}
