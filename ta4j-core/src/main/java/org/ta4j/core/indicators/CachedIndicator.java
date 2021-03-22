/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 Marc de Verdelhan, 2017-2019 Ta4j Organization & respective
 * authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ta4j.core.indicators;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Cached {@link Indicator indicator}.
 *
 * Caches the constructor of the indicator. Avoid to calculate the same index of
 * the indicator twice.
 */
public abstract class CachedIndicator<T> extends AbstractIndicator<T> {

    private static final long serialVersionUID = 7505855220893125595L;

    /**
     * List of cached results
     */
    private final List<T> results = new ArrayList<T>();

    /**
     * Should always be the index of the last result in the results list. I.E. the
     * last calculated result.
     */
    protected int highestResultIndex = -1;

    /**
     * Constructor.
     *
     * @param series the related bar series
     */
    public CachedIndicator(BarSeries series) {
        super(series);
    }

    /**
     * Constructor.
     *
     * @param indicator a related indicator (with a bar series)
     */
    public CachedIndicator(Indicator<?> indicator) {
        this(indicator.getBarSeries());
    }

    private boolean sizeInitialized = false;

    @Override
    public T getValue(int index) {
        BarSeries series = getBarSeries();
        if (series == null) {
            // Series is null; the indicator doesn't need cache.
            // (e.g. simple computation of the value)
            // --> Calculating the value
            return calculate(index);
        }

        if(!this.sizeInitialized) {
            initializeLength();
        }

        T result = this.results.get(index);
        if (result == null) {
            result = calculate(index);
            results.set(index, result);
            if(index > this.highestResultIndex)
                this.highestResultIndex = index;
        }
        return result;
    }

    private synchronized void initializeLength() {
        if(!this.sizeInitialized) {
            assert results.isEmpty() : "Cache results list should be empty";
            results.addAll(Collections.nCopies(this.getBarSeries().getBarData().size(), null));
            this.sizeInitialized = true;
        }
    }

    /**
     * @param index the bar index
     * @return the value of the indicator
     */
    protected abstract T calculate(int index);
}