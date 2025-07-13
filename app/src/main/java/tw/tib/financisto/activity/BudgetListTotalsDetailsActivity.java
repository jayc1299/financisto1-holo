/*
 * Copyright (c) 2012 Denis Solonenko.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package tw.tib.financisto.activity;

import android.content.Intent;
import tw.tib.financisto.R;
import tw.tib.financisto.filter.WhereFilter;
import tw.tib.financisto.db.BudgetsTotalCalculator;
import tw.tib.financisto.model.Budget;
import tw.tib.financisto.model.Total;

import java.util.List;

public class BudgetListTotalsDetailsActivity extends AbstractTotalsDetailsActivity  {

    private WhereFilter filter = WhereFilter.empty();
    private BudgetsTotalCalculator calculator;

    public BudgetListTotalsDetailsActivity() {
        super(R.string.budget_total_in_currency);
    }

    @Override
    protected void internalOnCreate() {
        Intent intent = getIntent();
        if (intent != null) {
            filter = WhereFilter.fromIntent(intent);
        }
    }

    @Override
    protected void prepareInBackground() {
        List<Budget> budgets = db.getAllBudgets(filter, null);
        calculator = new BudgetsTotalCalculator(db, budgets);
        calculator.updateBudgets(null);
    }

    protected Total[] getTotals() {
        return calculator.calculateTotals();
    }

}
