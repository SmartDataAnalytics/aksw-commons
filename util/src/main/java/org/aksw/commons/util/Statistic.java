/**
 * Copyright (C) 2011, SAIM team at the MOLE research
 * group at AKSW / University of Leipzig
 *
 * This file is part of SAIM (Semi-Automatic Instance Matcher).
 *
 * SAIM is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * SAIM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.aksw.commons.util;

/** The class Statistic contains methods for performing basic statistic operations such as the fScore.
 *  @author Konrad Höffner */
public class Statistic
{
	
	/** Calculates the f score of a precision-recall-pair where the parameter beta controls 
	 * the weighing of precision against recall (use 1.0 for the original f or f1-score with equal weights).
	 *  See also <a href="http://en.wikipedia.org/wiki/F_score">http://en.wikipedia.org/wiki/F_score</a>.
	 * @param precision the fraction of retrieved instances that are relevant
	 * @param recall the fraction of relevant instances that are retrieved
	 * @param beta A value of 1.0 means equal weights. Higher weights of precision are achieved by using a smaller value of beta,
	 * this is for example useful for semantic web owl:sameAs links.
	 * Higher weights of recall are achieved by bigger values of beta.
	 * @return the resulting f score
	 */
	public static double fScore(double precision, double recall, double beta)
	{
		double beta2 = beta*beta;
		return (1+beta2)*precision*recall/(beta2*precision+recall);
	}
}