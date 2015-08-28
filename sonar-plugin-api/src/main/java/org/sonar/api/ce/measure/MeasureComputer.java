/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.sonar.api.ce.measure;

import java.util.List;
import java.util.Set;
import javax.annotation.CheckForNull;
import org.sonar.api.ExtensionPoint;
import org.sonar.api.ce.measure.MeasureComputer.MeasureComputerDefinition.Builder;
import org.sonar.api.server.ServerSide;

/**
 * Define how to compute new measures on some metrics declared by {@link org.sonar.api.measures.Metrics}.
 * <p/>
 * This interface replaces the deprecated class org.sonar.api.batch.Decorator.
 * <p/>
 * <h3>How to use</h3>
 * <pre>
 * public class MyMeasureComputer implements MeasureComputer {
 *
 *   {@literal @}Override
 *   public MeasureComputerDefinition define(MeasureComputerDefinitionContext defContext) {
 *     return defContext.newDefinitionBuilder()
 *
 *     // Input metrics can be empty, for instance if only issues will be read
 *     .setInputMetrics("ncloc")
 *
 *     // Output metrics must contains at least one metric
 *     .setOutputMetrics("my_new_metric")
 *
 *     .build();
 *   }
 *
 *   {@literal @}Override
 *   public void compute(MeasureComputerContext context) {
 *     int ncloc = context.getMeasure("ncloc");
 *     List&lt;Issue&gt; issues = context.getIssues();
 *     if (ncloc != null && !issues.isEmpty()) {
 *       double value = issues.size() / ncloc;
 *       context.addMeasure("my_new_metric", value);
 *     }
 *   }
 * }
 * </pre>
 * <p/>
 * <h3>How to test</h3>
 * <pre>
 * public class MyMeasureComputerTest {
 *
 *   MyMeasureComputer underTest = new MyMeasureComputer();
 *
 *   @Test
 *   public void test_definition() {
 *     TestMeasureComputerDefinitionContext defContext = new TestMeasureComputerDefinitionContext();
 *     MeasureComputerDefinition def = underTest.define(defContext);
 *     assertThat(def).isNotNull();
 *     assertThat(def.getInputMetrics()).containsOnly("ncloc");
 *     assertThat(def.getOutputMetrics()).containsOnly("my_new_metric");
 *   }
 *
 *   @Test
 *   public void sum_ncloc_and_issues() {
 *     TestMeasureComputerContext context = new TestMeasureComputerContext(underTest);
 *     context.addMeasure("ncloc", 2);
 *     context.setIssues(Arrays.asList(new TestIssue.Builder().setKey("ABCD").build()));
 *     underTest.compute(context);
 *
 *     assertThat(context.getMeasureValue("my_new_metric")).isEqualTo(0.5);
 *   }
 * </pre>
 *
 * @since 5.2
 */
@ServerSide
@ExtensionPoint
public interface MeasureComputer {

  /**
   * Use to define which metrics are required to compute some measures on some given metrics
   */
  MeasureComputerDefinition define(MeasureComputerDefinitionContext defContext);

  /**
   * Context specific to the definition of the measure computer
   */
  interface MeasureComputerDefinitionContext {
    Builder newDefinitionBuilder();
  }

  interface MeasureComputerDefinition {
    /**
     * Return the metric keys that can be read using {@link MeasureComputerContext}.
     * Can be empty for instance when the computer only need to access to issues.
     */
    Set<String> getInputMetrics();

    /**
     * Return the metric keys that can be create using {@link MeasureComputerContext}.
     * Can never ne empty.
     */
    Set<String> getOutputMetrics();

    interface Builder {
      /**
       * Input metrics can be empty (for instance when only issues are needed)
       * @throws NullPointerException if inputMetrics is null
       * @throws NullPointerException if the metrics contains a {@code null}
       * */
      Builder setInputMetrics(String... inputMetrics);

      /**
       * @throws NullPointerException if outputMetrics is null
       * @throws IllegalArgumentException if there's not at least one output metrics
       * @throws NullPointerException if the metrics contains a {@code null}
       */
      Builder setOutputMetrics(String... outputMetrics);

      /**
       * @throws NullPointerException if inputMetrics is null
       * @throws NullPointerException if inputs metrics contains a {@code null}
       * @throws NullPointerException if outputMetrics is null
       * @throws IllegalArgumentException if there's not at least one output metrics
       * @throws NullPointerException if outputs metrics contains a {@code null}
       */
      MeasureComputerDefinition build();
    }
  }

  /**
   * This method will be called on each component of the projects.
   */
  void compute(MeasureComputerContext context);

  /**
   * Context specific to the computation of the measure(s) of a given component
   */
  interface MeasureComputerContext {
    /**
     * Returns the current component.
     */
    Component getComponent();

    /**
     * Returns settings of the current component.
     */
    Settings getSettings();

    /**
     * Returns the measure from a given metric on the current component.
     *
     * @throws IllegalArgumentException if the metric is not listed in {@link MeasureComputerDefinition#getInputMetrics()}
     */
    @CheckForNull
    Measure getMeasure(String metric);

    /**
     * Returns measures from a given metric on children of the current component.
     * It no measure is found for a child, this measure is ignored
     *
     * @throws IllegalArgumentException if the metric is not listed in {@link MeasureComputerDefinition#getInputMetrics()} 
     * or in {@link MeasureComputerDefinition#getOutputMetrics()}
     */
    Iterable<Measure> getChildrenMeasures(String metric);

    /**
     * Add a new measure of a given metric which measure type will be int
     *
     * @throws IllegalArgumentException if the metric is not listed in {@link MeasureComputerDefinition#getOutputMetrics()}
     * @throws UnsupportedOperationException if a measure for the specified metric already exists for the current component
     */
    void addMeasure(String metric, int value);

    /**
     * Add a new measure of a given metric which measure type will be double
     *
     * @throws IllegalArgumentException if the metric is not listed in {@link MeasureComputerDefinition#getOutputMetrics()}
     * @throws UnsupportedOperationException if a measure for the specified metric already exists for the current component
     */
    void addMeasure(String metric, double value);

    /**
     * Add a new measure of a given metric which measure type will be long
     *
     * @throws IllegalArgumentException if the metric is not listed in {@link MeasureComputerDefinition#getOutputMetrics()}
     * @throws UnsupportedOperationException if a measure for the specified metric already exists for the current component
     */
    void addMeasure(String metric, long value);

    /**
     * Add a new measure of a given metric which measure type will be string
     *
     * @throws IllegalArgumentException if the metric is not listed in {@link MeasureComputerDefinition#getOutputMetrics()}
     * @throws UnsupportedOperationException if a measure for the specified metric already exists for the current component
     */
    void addMeasure(String metric, String value);

    /**
     * Add a new measure of a given metric which measure type will be boolean
     *
     * @throws IllegalArgumentException if the metric is not listed in {@link MeasureComputerDefinition#getOutputMetrics()}
     * @throws UnsupportedOperationException if a measure for the specified metric already exists for the current component
     */
    void addMeasure(String metric, boolean value);

    /**
     * Return list of all issues (open, closed, etc.) of current component.
     */
    List<? extends Issue> getIssues();
  }
}