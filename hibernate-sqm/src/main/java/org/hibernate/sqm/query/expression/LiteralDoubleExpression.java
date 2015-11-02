/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.BasicTypeDescriptor;
import org.hibernate.sqm.domain.StandardBasicTypeDescriptors;

/**
 * @author Steve Ebersole
 */
public class LiteralDoubleExpression extends AbstractLiteralExpressionImpl<Double> {
	public LiteralDoubleExpression(Double value) {
		this( value, StandardBasicTypeDescriptors.INSTANCE.DOUBLE );
	}

	public LiteralDoubleExpression(Double value, BasicTypeDescriptor typeDescriptor) {
		super( value, typeDescriptor );
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitLiteralDoubleExpression( this );
	}
}
