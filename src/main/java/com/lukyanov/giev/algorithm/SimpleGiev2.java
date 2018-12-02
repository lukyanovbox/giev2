package com.lukyanov.giev.algorithm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkState;
import static com.lukyanov.giev.algorithm.Gene.ONE;
import static com.lukyanov.giev.algorithm.Gene.ZERO;
import static com.lukyanov.giev.util.CoordinatesExecutor.mathFunctionExecutor;
import static com.lukyanov.giev.util.CoordinatesExecutor.rToXCordinateConverter;
import lombok.Builder;
import lombok.Getter;


@Builder
public class SimpleGiev2 {

   private static final int ITEMS_COUNT = 20000;
   final int populationSize;
   final int generationCount;

   final double crossingOverP;
   final double mutationP;

   double from;
   double to;

   @Getter
   volatile int currentPopulationNumber = 0;
   List<Indidvid> population;


   final Function<Integer, Chromosome> chromosomeFinisher = r ->
         Chromosome.builder()
               .value(r)
               .toXCordinateConverter(rToXCordinateConverter(from, to, ITEMS_COUNT))
               .build();

   final Supplier<Chromosome> chromosomeGenerator = () -> chromosomeFinisher.apply(RandomUtils.nextInt(0, ITEMS_COUNT));

   final Supplier<Indidvid> individGenerator = () -> Indidvid.builder()
         .chromosome1(chromosomeGenerator.get())
         .chromosome2(chromosomeGenerator.get())
         .mathFunctionExecutor(mathFunctionExecutor)
         .build();

   final BiFunction<Integer, Integer, Indidvid> individFinisher = (ch1value, ch2value) ->
         Indidvid.builder()
               .chromosome1(chromosomeFinisher.apply(ch1value))
               .chromosome2(chromosomeFinisher.apply(ch2value))
               .mathFunctionExecutor(mathFunctionExecutor)
               .build();

   public Double getMin() {
      return population.stream()
            .mapToDouble(Indidvid::executeFunctionValue)
            .min()
            .getAsDouble();
   }

   public Double getAverage() {
      return population.stream()
            .mapToDouble(Indidvid::executeFunctionValue)
            .average()
            .getAsDouble();
   }


   public boolean hasNext() {
      return currentPopulationNumber < generationCount;
   }


   public List<Pair<Double, Double>> generateNextPopulation() {
      if (currentPopulationNumber == 0) {
         population = Stream.generate(individGenerator)
               .limit(populationSize)
               .collect(Collectors.toList());
      }
      else {
         population = evolutionPopulation(population);
      }
      currentPopulationNumber++;

      return population.stream()
            .map(i -> Pair.of(i.chromosome1.getX(), i.chromosome2.getX()))
            .collect(Collectors.toList());
   }





   private List<Indidvid> evolutionPopulation(List<Indidvid> population) {
      checkState(population.size() == populationSize,
            String.format("Actual size is %s", population.size()));

      List<Indidvid> intermediatePopulation = generateIntermediatePopulation(population);

      checkState(intermediatePopulation.size() == populationSize,
            String.format("Actual size is %s", intermediatePopulation.size()));

      List<Indidvid> newPopulation = selectAndCrossingOver(intermediatePopulation);

      checkState(newPopulation.size() == populationSize,
            String.format("Actual size is %s", newPopulation.size()));

      return mutate(newPopulation);
   }

   private List<Indidvid> generateIntermediatePopulation(List<Indidvid> population) {
      final Double maxFValue = population.stream()
            .mapToDouble(Indidvid::executeFunctionValue)
            .max()
            .getAsDouble();

      final Double sumFcVal = population.stream()
            .mapToDouble(Indidvid::executeFunctionValue)
            .sum();

      Map<Indidvid, Double> pIndividMap = population.stream()
            .map(i -> Pair.of(i, ((1 + maxFValue - i.executeFunctionValue()) / sumFcVal)))
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));


      double var = 0;
      Map<Double, Indidvid> rouletteMap = new HashMap<>();

      for (Map.Entry<Indidvid, Double> entry : pIndividMap.entrySet()) {
         var += entry.getValue();
         rouletteMap.put(var, entry.getKey());
      }

      //      checkState(rouletteMap.values().size() == populationSize,
      //            String.format("Actual size is %s", rouletteMap.size()));


      List<Indidvid> intermediateIndividList = new ArrayList<>();
      for (int i = 0; i < population.size(); i++) {
         double rnd = RandomUtils.nextDouble(0,
               rouletteMap.keySet().stream().max(Comparator.comparing(Double::doubleValue)).get());
         for (Double key : rouletteMap.keySet().stream().sorted().collect(Collectors.toList())) {
            if (rnd < key) {
               intermediateIndividList.add(rouletteMap.get(key));
               break;
            }
         }
      }

      return intermediateIndividList;
   }


   private List<Indidvid> selectAndCrossingOver(List<Indidvid> intermediatePopulation) {
      List<Indidvid> newPopulation = new ArrayList<>();
      for (int i = 0; i < intermediatePopulation.size() / 2; i++) {
         int firstItemIndex = RandomUtils.nextInt(0, intermediatePopulation.size());
         int secondItemIndex = firstItemIndex;
         while (secondItemIndex == firstItemIndex) {
            secondItemIndex = RandomUtils.nextInt(0, intermediatePopulation.size());
         }
         if (RandomUtils.nextDouble(0, 1) < crossingOverP) {
            newPopulation.addAll(
                  crossOver(intermediatePopulation.get(firstItemIndex), intermediatePopulation.get(secondItemIndex)));
         }
         else {
            newPopulation.add(individFinisher.apply(
                  intermediatePopulation.get(firstItemIndex).chromosome1.value,
                  intermediatePopulation.get(firstItemIndex).chromosome2.value)
            );
            newPopulation.add(individFinisher.apply(
                  intermediatePopulation.get(secondItemIndex).chromosome1.value,
                  intermediatePopulation.get(secondItemIndex).chromosome2.value)
            );
         }
      }

      if (intermediatePopulation.size() % 2 != 0) {
         int rndIndex = RandomUtils.nextInt(0, intermediatePopulation.size());

         newPopulation.add(individFinisher.apply(
               intermediatePopulation.get(rndIndex).chromosome1.value,
               intermediatePopulation.get(rndIndex).chromosome2.value)
         );
      }

      return newPopulation;
   }


   private List<Indidvid> crossOver(Indidvid first, Indidvid second) {
      return ImmutableList.of(
            individFinisher.apply(first.chromosome1.value, second.chromosome2.value),
            individFinisher.apply(second.chromosome1.value, first.chromosome2.value)
      );
   }


   private List<Indidvid> mutate(List<Indidvid> population) {
      for (Indidvid individ : population) {
         double rnd = RandomUtils.nextDouble(0, 1);

         if (rnd < mutationP) {

            if (RandomUtils.nextInt(0, 2) == 0) {
               individ.chromosome1.value = individ.chromosome1.value + 1;
            }
            else {
               individ.chromosome2.value = individ.chromosome2.value + 1;

            }
         }
      }

      return population;
   }
}
