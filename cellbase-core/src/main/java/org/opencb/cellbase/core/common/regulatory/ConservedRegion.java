/*
 * Copyright 2015 OpenCB
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
 */

package org.opencb.cellbase.core.common.regulatory;

// Generated Jun 5, 2012 6:41:13 PM by Hibernate Tools 3.4.0.CR1


/**
 * ConservedRegion generated by hbm2java
 */
public class ConservedRegion implements java.io.Serializable {

	private int conservedRegionId;
	private String chromosome;
	private int start;
	private int end;
	private String strand;
	private int length;
	private double lowerLimitVertebrate;
	private double dataRangeVertebrate;
	private double sumDataVertebrate;
	private double sumSquareVertebrate;
	private double lowerLimitMammal;
	private double dataRangeMammal;
	private double sumDataMammal;
	private double sumSquareMammal;
	private double lowerLimitPrimate;
	private double dataRangePrimate;
	private double sumDataPrimate;
	private double sumSquarePrimate;
	private String method;

	public ConservedRegion() {
	}

	public ConservedRegion(int conservedRegionId, String chromosome, int start,
			int end, String strand, int length, double lowerLimitVertebrate,
			double dataRangeVertebrate, double sumDataVertebrate,
			double sumSquareVertebrate, double lowerLimitMammal,
			double dataRangeMammal, double sumDataMammal,
			double sumSquareMammal, double lowerLimitPrimate,
			double dataRangePrimate, double sumDataPrimate,
			double sumSquarePrimate, String method) {
		this.conservedRegionId = conservedRegionId;
		this.chromosome = chromosome;
		this.start = start;
		this.end = end;
		this.strand = strand;
		this.length = length;
		this.lowerLimitVertebrate = lowerLimitVertebrate;
		this.dataRangeVertebrate = dataRangeVertebrate;
		this.sumDataVertebrate = sumDataVertebrate;
		this.sumSquareVertebrate = sumSquareVertebrate;
		this.lowerLimitMammal = lowerLimitMammal;
		this.dataRangeMammal = dataRangeMammal;
		this.sumDataMammal = sumDataMammal;
		this.sumSquareMammal = sumSquareMammal;
		this.lowerLimitPrimate = lowerLimitPrimate;
		this.dataRangePrimate = dataRangePrimate;
		this.sumDataPrimate = sumDataPrimate;
		this.sumSquarePrimate = sumSquarePrimate;
		this.method = method;
	}

	public int getConservedRegionId() {
		return this.conservedRegionId;
	}

	public void setConservedRegionId(int conservedRegionId) {
		this.conservedRegionId = conservedRegionId;
	}

	public String getChromosome() {
		return this.chromosome;
	}

	public void setChromosome(String chromosome) {
		this.chromosome = chromosome;
	}

	public int getStart() {
		return this.start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return this.end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public String getStrand() {
		return this.strand;
	}

	public void setStrand(String strand) {
		this.strand = strand;
	}

	public int getLength() {
		return this.length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public double getLowerLimitVertebrate() {
		return this.lowerLimitVertebrate;
	}

	public void setLowerLimitVertebrate(double lowerLimitVertebrate) {
		this.lowerLimitVertebrate = lowerLimitVertebrate;
	}

	public double getDataRangeVertebrate() {
		return this.dataRangeVertebrate;
	}

	public void setDataRangeVertebrate(double dataRangeVertebrate) {
		this.dataRangeVertebrate = dataRangeVertebrate;
	}

	public double getSumDataVertebrate() {
		return this.sumDataVertebrate;
	}

	public void setSumDataVertebrate(double sumDataVertebrate) {
		this.sumDataVertebrate = sumDataVertebrate;
	}

	public double getSumSquareVertebrate() {
		return this.sumSquareVertebrate;
	}

	public void setSumSquareVertebrate(double sumSquareVertebrate) {
		this.sumSquareVertebrate = sumSquareVertebrate;
	}

	public double getLowerLimitMammal() {
		return this.lowerLimitMammal;
	}

	public void setLowerLimitMammal(double lowerLimitMammal) {
		this.lowerLimitMammal = lowerLimitMammal;
	}

	public double getDataRangeMammal() {
		return this.dataRangeMammal;
	}

	public void setDataRangeMammal(double dataRangeMammal) {
		this.dataRangeMammal = dataRangeMammal;
	}

	public double getSumDataMammal() {
		return this.sumDataMammal;
	}

	public void setSumDataMammal(double sumDataMammal) {
		this.sumDataMammal = sumDataMammal;
	}

	public double getSumSquareMammal() {
		return this.sumSquareMammal;
	}

	public void setSumSquareMammal(double sumSquareMammal) {
		this.sumSquareMammal = sumSquareMammal;
	}

	public double getLowerLimitPrimate() {
		return this.lowerLimitPrimate;
	}

	public void setLowerLimitPrimate(double lowerLimitPrimate) {
		this.lowerLimitPrimate = lowerLimitPrimate;
	}

	public double getDataRangePrimate() {
		return this.dataRangePrimate;
	}

	public void setDataRangePrimate(double dataRangePrimate) {
		this.dataRangePrimate = dataRangePrimate;
	}

	public double getSumDataPrimate() {
		return this.sumDataPrimate;
	}

	public void setSumDataPrimate(double sumDataPrimate) {
		this.sumDataPrimate = sumDataPrimate;
	}

	public double getSumSquarePrimate() {
		return this.sumSquarePrimate;
	}

	public void setSumSquarePrimate(double sumSquarePrimate) {
		this.sumSquarePrimate = sumSquarePrimate;
	}

	public String getMethod() {
		return this.method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

}
