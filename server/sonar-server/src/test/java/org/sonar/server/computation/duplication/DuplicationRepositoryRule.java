/*
 * SonarQube
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.computation.duplication;

import java.util.Set;
import org.junit.rules.ExternalResource;
import org.sonar.server.computation.batch.TreeRootHolderRule;
import org.sonar.server.computation.component.Component;
import org.sonar.server.computation.component.ComponentProvider;
import org.sonar.server.computation.component.TreeRootHolder;
import org.sonar.server.computation.component.TreeRootHolderComponentProvider;

public class DuplicationRepositoryRule extends ExternalResource implements DuplicationRepository {
  private final TreeRootHolder treeRootHolder;
  private final ComponentProvider componentProvider;
  private DuplicationRepositoryImpl delegate;

  private DuplicationRepositoryRule(TreeRootHolder treeRootHolder) {
    this.treeRootHolder = treeRootHolder;
    this.componentProvider = new TreeRootHolderComponentProvider(treeRootHolder);
  }

  public static DuplicationRepositoryRule create(TreeRootHolderRule treeRootHolder) {
    return new DuplicationRepositoryRule(treeRootHolder);
  }

  @Override
  protected void before() throws Throwable {
    this.delegate = new DuplicationRepositoryImpl(treeRootHolder);
  }

  @Override
  protected void after() {
    this.componentProvider.reset();
    this.delegate = null;
  }

  public Set<Duplication> getDuplications(int fileRef) {
    componentProvider.ensureInitialized();

    return delegate.getDuplications(componentProvider.getByRef(fileRef));
  }

  public DuplicationRepositoryRule addDuplication(int fileRef, TextBlock original, TextBlock duplicate) {
    componentProvider.ensureInitialized();

    delegate.addDuplication(componentProvider.getByRef(fileRef), original, duplicate);

    return this;
  }

  public DuplicationRepositoryRule addDuplication(int fileRef, TextBlock original, int otherFileRef, TextBlock duplicate) {
    componentProvider.ensureInitialized();

    delegate.addDuplication(componentProvider.getByRef(fileRef), original, componentProvider.getByRef(otherFileRef), duplicate);

    return this;
  }

  public DuplicationRepositoryRule addDuplication(int fileRef, TextBlock original, String otherFileKey, TextBlock duplicate) {
    componentProvider.ensureInitialized();

    delegate.addDuplication(componentProvider.getByRef(fileRef), original, otherFileKey, duplicate);

    return this;
  }

  @Override
  public Set<Duplication> getDuplications(Component file) {
    return delegate.getDuplications(file);
  }

  @Override
  public void addDuplication(Component file, TextBlock original, TextBlock duplicate) {
    delegate.addDuplication(file, original, duplicate);
  }

  @Override
  public void addDuplication(Component file, TextBlock original, Component otherFile, TextBlock duplicate) {
    delegate.addDuplication(file, original, otherFile, duplicate);
  }

  @Override
  public void addDuplication(Component file, TextBlock original, String otherFileKey, TextBlock duplicate) {
    delegate.addDuplication(file, original, otherFileKey, duplicate);
  }
}
