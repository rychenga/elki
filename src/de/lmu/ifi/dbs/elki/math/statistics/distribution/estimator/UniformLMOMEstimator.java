package de.lmu.ifi.dbs.elki.math.statistics.distribution.estimator;

/*
 This file is part of ELKI:
 Environment for Developing KDD-Applications Supported by Index-Structures

 Copyright (C) 2013
 Ludwig-Maximilians-Universität München
 Lehr- und Forschungseinheit für Datenbanksysteme
 ELKI Development Team

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import de.lmu.ifi.dbs.elki.math.statistics.distribution.UniformDistribution;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.AbstractParameterizer;

/**
 * Estimate the parameters of a normal distribution using the L-Moments.
 * 
 * @author Erich Schubert
 * 
 * @apiviz.has UniformDistribution
 */
public class UniformLMOMEstimator extends AbstractLMOMEstimator<UniformDistribution> {
  /**
   * Static instance
   */
  public static final UniformLMOMEstimator STATIC = new UniformLMOMEstimator();

  /**
   * Constructor. Private: use static instance.
   */
  private UniformLMOMEstimator() {
    super();
  }

  @Override
  public int getNumMoments() {
    return 2;
  }

  @Override
  public UniformDistribution estimateFromLMoments(double[] xmom) {
    return new UniformDistribution(xmom[0] - 3 * xmom[1], xmom[0] + 3 * xmom[1]);
  }

  @Override
  public Class<? super UniformDistribution> getDistributionClass() {
    return UniformDistribution.class;
  }

  /**
   * Parameterization class.
   * 
   * @author Erich Schubert
   * 
   * @apiviz.exclude
   */
  public static class Parameterizer extends AbstractParameterizer {
    @Override
    protected UniformLMOMEstimator makeInstance() {
      return STATIC;
    }
  }
}
