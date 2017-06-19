/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.coffig.mapper;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.ValueNode;
import org.seedstack.coffig.spi.ConfigurationComponent;
import org.seedstack.coffig.spi.ConfigurationMapper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static org.seedstack.coffig.util.Utils.instantiateDefault;

public class MapMapper implements ConfigurationMapper {
    private Coffig coffig;

    @Override
    public void initialize(Coffig coffig) {
        this.coffig = coffig;
    }

    @Override
    public ConfigurationComponent fork() {
        return new MapMapper();
    }

    @Override
    public boolean canHandle(Type type) {
        if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            if (rawType instanceof Class) {
                return Map.class.isAssignableFrom(((Class<?>) rawType));
            }
        }
        return false;
    }

    @Override
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Cast is verified in canHandle() method")
    public Object map(TreeNode treeNode, Type type) {
        Type keyType = ((ParameterizedType) type).getActualTypeArguments()[0];
        Type valueType = ((ParameterizedType) type).getActualTypeArguments()[1];

        if (treeNode.type() == TreeNode.Type.MAP_NODE) {
            return treeNode.namedNodes()
                    .collect(toMap(
                            namedNode -> coffig.getMapper().map(new ValueNode(namedNode.name()), keyType),
                            namedNode -> coffig.getMapper().map(namedNode.node(), valueType)
                    ));
        } else {
            return treeNode.nodes()
                    .collect(toMap(
                            node -> coffig.getMapper().map(node, keyType),
                            node -> instantiateDefault((Class<?>) valueType))
                    );
        }
    }

    @Override
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Cast is verified in canHandle() method")
    public TreeNode unmap(Object object, Type type) {
        MapNode mapNode = new MapNode();
        Type valueType = ((ParameterizedType) type).getActualTypeArguments()[1];
        ((Map<?, ?>) object).forEach((key, value) -> {
            if (key != null) {
                mapNode.set(String.valueOf(key), coffig.getMapper().unmap(value, valueType));
            }
        });
        return mapNode;
    }
}
