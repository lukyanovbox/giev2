package com.lukyanov.giev.algorithm;

import java.util.List;
import java.util.function.Function;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;


@Builder
@Data
public class Chromosome {

   int value;

   private final Function<Double, Double> toXCordinateConverter;


   public Double getX() {
      return toXCordinateConverter
            .apply((double)value);
   }
}
