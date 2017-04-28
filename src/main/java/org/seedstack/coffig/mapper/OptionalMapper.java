/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.spi.ConfigurationMapper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

public class OptionalMapper implements ConfigurationMapper {
    private Coffig coffig;

    @Override
    public void initialize(Coffig coffig) {
        this.coffig = coffig;
    }

    @Override
    public boolean canHandle(Type type) {
        if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            if (rawType instanceof Class) {
                return Optional.class.isAssignableFrom(((Class<?>) rawType));
            }
        }
        return false;
    }

    @Override
    public Object map(TreeNode treeNode, Type type) {
        Type itemType = ((ParameterizedType) type).getActualTypeArguments()[0];
        return Optional.ofNullable(coffig.getMapper().map(treeNode, itemType));
    }

    @Override
    public TreeNode unmap(Object object, Type type) {
        Type itemType = ((ParameterizedType) type).getActualTypeArguments()[0];
        if (((Optional<?>) object).isPresent()) {
            return coffig.getMapper().unmap(((Optional<?>) object).get(), itemType);
        } else {
            return null;
        }
    }
}
