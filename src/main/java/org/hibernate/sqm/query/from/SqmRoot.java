/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.from;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.SqmExpressableTypeEntity;
import org.hibernate.sqm.query.expression.domain.SqmEntityBinding;

/**
 * @author Steve Ebersole
 */
public class SqmRoot extends AbstractSqmFrom {
	public SqmRoot(
			SqmFromElementSpace fromElementSpace,
			String uid,
			String alias,
			SqmExpressableTypeEntity entityReference) {
		super(
				fromElementSpace,
				uid,
				alias,
				new SqmEntityBinding( entityReference ),
				entityReference
		);

		getBinding().injectExportedFromElement( this );
	}

	@Override
	public SqmEntityBinding getBinding() {
		return (SqmEntityBinding) super.getBinding();
	}

	public String getEntityName() {
		return getBinding().getBoundNavigable().getEntityName();
	}

	@Override
	public SqmExpressableTypeEntity getIntrinsicSubclassIndicator() {
		// a root FromElement cannot indicate a subclass intrinsically (as part of its declaration)
		return null;
	}

	@Override
	public String toString() {
		return getEntityName() + " as " + getIdentificationVariable();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitRootEntityFromElement( this );
	}
}
