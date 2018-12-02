package com.lukyanov.giev.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.lukyanov.giev.algorithm.Gene;

import static com.lukyanov.giev.algorithm.Gene.ONE;
import static java.lang.Math.pow;


public class CoordinatesExecutor {

   public static final ToDoubleBiFunction<Double, Double> mathFunctionExecutor = (x, y) -> pow((x * x) + (y * y), 0.25) * (
         Math.pow(Math.sin(50 * pow(x * x + y * y, 0.1)), 2) + 1);

   public static Function<Double, Double> rToXCordinateConverter(final double from, final double to, int itemsCount) {
      return r -> from + r * (to - from) / itemsCount;
   }


   public static List<List<Pair<Double, Double>>> generateXYPairs(final double from, final double to, final double step) {
      List<List<Number>> xyzList = new ArrayList<>();



      for (double x1 = from; x1 < to; x1 = x1 + step) {
         for (double x2 = from; x2 < to; x2 = x2 + step) {
            int z = (int) mathFunctionExecutor.applyAsDouble(x1, x2);
            xyzList.add(ImmutableList.of(x1, x2, z));
         }
      }

      double zMin = xyzList.stream().mapToDouble(l -> (Integer) l.get(2)).min().getAsDouble();
      double zMax = xyzList.stream().mapToDouble(l -> (Integer) l.get(2)).max().getAsDouble();

      List<List<Pair<Double, Double>>> resultList = new ArrayList<>();

      List<Integer> zValues = xyzList.stream().map(l -> (Integer) l.get(2)).distinct().sorted().collect(Collectors.toList());
      for (int i = 0; i < zValues.size(); i = i + zValues.size() / 5) {

         int currz = zValues.get(i);
         resultList.add(xyzList.stream().filter(l -> (Integer) l.get(2) == currz)
               .map(l -> Pair.of((Double) l.get(0), (Double) l.get(1))).collect(
                     Collectors.toList()));
      }

      return resultList;
   }





}
