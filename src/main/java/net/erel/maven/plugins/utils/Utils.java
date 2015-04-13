package net.erel.maven.plugins.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.erel.maven.plugins.service.git.PGXBranches;

import org.codehaus.plexus.util.xml.Xpp3Dom;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.common.primitives.Primitives;

public class Utils {

  

  public static List<Field> getAllFields(List<Field> fields, Class<?> type) {
    for (Field field : type.getDeclaredFields()) {
      if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())
          && (!java.lang.reflect.Modifier.isFinal(field.getModifiers()) || (field.getDeclaringClass()
              .isAssignableFrom(Iterable.class))) || field.getDeclaringClass().equals(Xpp3Dom.class)) {
        fields.add(field);
      }
    }

    if (type.getSuperclass() != null) {
      fields = getAllFields(fields, type.getSuperclass());
    }

    return fields;
  }

  public static void cleanUpModel(Object obj, String stringBit) {
    try {
      pruneModel(obj, stringBit, new ArrayList<>());
    } catch (IllegalArgumentException e) {
      // smodel may be dirty...
    } catch (IllegalAccessException e) {
      // model may be dirty...
    }
  }

  private static void pruneModel(Object obj, String stringBit, Collection<Object> parents)
      throws IllegalArgumentException, IllegalAccessException {

    if (obj == null) {
      return;
    }

    if (parents.contains(obj)) {
      return;
    }

    Set<Object> newparents = Sets.newHashSet(parents);
    newparents.add(obj);

    for (Field field : getAllFields(new ArrayList<Field>(), obj.getClass())) {
      field.setAccessible(true);
      Object fieldValue = field.get(obj);

      if (fieldValue == null || Primitives.isWrapperType(fieldValue.getClass())) {

        continue;
      } else if (fieldValue instanceof String) {

        if (((String) fieldValue).contains(stringBit)) {

          field.set(obj, null);
        }

      } else if (fieldValue instanceof Iterable) {
        Iterable<?> fieldValueIterables = (Iterable<?>) fieldValue;
        for (Object fieldValueIterable : fieldValueIterables) {

          pruneModel(fieldValueIterable, stringBit, newparents);
        }

      } else if (fieldValue.getClass().isArray()) {

        for (int i = 0; i < ((Object[]) fieldValue).length; i++) {
          Object item = ((Object[]) fieldValue)[i];
          if (item != null) {

            pruneModel(item, stringBit, newparents);
          } else {

          }

        }

      }

      else {

        pruneModel(fieldValue, stringBit, newparents);
      }
    }

  }
}
