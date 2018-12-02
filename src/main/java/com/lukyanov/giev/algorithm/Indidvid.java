package com.lukyanov.giev.algorithm;

import java.util.function.ToDoubleBiFunction;

import lombok.Builder;
import lombok.Data;


@Builder
@Data
public class Indidvid {
   Chromosome chromosome1;
   Chromosome chromosome2;

   ToDoubleBiFunction<Double, Double> mathFunctionExecutor;

   public Double executeFunctionValue() {
      return mathFunctionExecutor.applyAsDouble(chromosome1.getX(), chromosome2.getX());
   }
}
