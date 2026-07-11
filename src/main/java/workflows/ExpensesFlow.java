package workflows;

import extensions.UiActions;
import extensions.Verifications;
import io.qameta.allure.Allure;
import pageObjects.*;
import utilities.*;
import static utilities.ManagePages.page;

public class ExpensesFlow {

    // Per-thread state accumulated while addExpenses() walks through one category's rows
    private static class ExpenseState {
        float totalExpense = 0f;
        int tabIndex = 0;
        String category = "";
        boolean newTab = true;
    }

    private static final ThreadLocal<ExpenseState> state = ThreadLocal.withInitial(ExpenseState::new);

    public static void addExpenses(String category, String[] names, String[] amounts) {
        Allure.step("Add expenses flow", () -> {
            ExpenseState st = state.get();
            st.category = category;

            for (int i = 0; i < names.length; i++) {
                // Navigate to next pages
                if (names[i].equals("nextButton")) {
                    UiActions.click(page(IncomesPage.class).nextButton());
                    WaitForElement.waitUntilUrlContains("/FinancialPlanMaker/emergencyFund");
                    continue;
                }
                // Switch to next tab
                if (names[i].equals("nextTab")) {
                    st.tabIndex++;
                    st.newTab = true;
                    var tab = page(PeriodExpensesPage.class).buttonsTab().get(st.tabIndex);
                    WaitForElement.waitFor(tab, "ELEMENT_CLICKABLE", 10);
                    UiActions.moveToElement(tab, 200);
                    UiActions.click(tab);
                    continue;
                }
                // At first a large button appears in a new tab; afterward an icon button appears
                if (st.newTab) {
                    st.newTab = false;
                    UiActions.click(page(IncomesPage.class).addIncomeOrExpense());
                } else {
                    try {
                        WaitForElement.waitFor(page(PeriodExpensesPage.class).MuiDialog(), "ELEMENT_INVISIBLE", 10);
                    } finally {
                        UiActions.click(page(PeriodExpensesPage.class).addExpenseIcon());
                    }
                }
                //Add the category details include name and amount.
                UiActions.click(page(PeriodExpensesPage.class).category());
                UiActions.click(page(PeriodExpensesPage.class).listOfValues(st.category));
                UiActions.enterText(page(IncomesPage.class).name(), names[i]);
                UiActions.enterText(page(IncomesPage.class).amount(), amounts[i]);
                UiActions.click(page(IncomesPage.class).add());

                st.totalExpense += Float.parseFloat(amounts[i]);
            }
        });
    }

    public static void emergencyFund(String amount, String currentAmount, String liquidAmount, String month) {
    	
        Allure.step("Add emergency fund flow", () -> {
        	
            UiActions.click(page(EmergencyFundPage.class).createButton());
            UiActions.enterText(page(EmergencyFundPage.class).desiredAmount(), amount);
            UiActions.enterText(page(EmergencyFundPage.class).currentAmount(), currentAmount);
            UiActions.enterText(page(IncomesPage.class).name(), liquidAmount);
            UiActions.enterText(page(EmergencyFundPage.class).saveMonths(), month);
            UiActions.click(page(IncomesPage.class).add());
            
            String monthlySavings = UiActions.getText(page(EmergencyFundPage.class).monthlySavings());
            int actualAmount = (Integer.parseInt(amount) - Integer.parseInt(currentAmount)) / Integer.parseInt(month);
            
            int offset = Integer.parseInt(monthlySavings) - actualAmount;
            if (Math.abs(offset) <= 10) {
                actualAmount += offset;
            }
            Verifications.verifyText(String.valueOf(actualAmount), monthlySavings);
            UiActions.click(page(IncomesPage.class).nextButton());
            WaitForElement.waitUntilUrlContains("/FinancialPlanMaker/recurringExpenses");
        });
    }
    
    public static void additionExpenses(String[] name, String[] targetAmount, String[] currentAmount,
            String[] liquidAmount, String[] years, String testcaseName) {
    	
        Allure.step("Add " + testcaseName + " flow", () -> {
        	      
            for (int i = 0; i < targetAmount.length; i++) {
                if (name[i].equals("Finish")) {
                    UiActions.click(page(FinancialGoalsPage.class).finishButton());
                    WaitForElement.waitUntilUrlContains("/congratulation");
                    continue;
                }
            
                if (name[i].equals("Update")) {
                    UiActions.click(page(RecurringExpensesPage.class).editButtons().get(0));
                }
               else if (name[i].equals("Delete")) {
                   WaitForElement.waitForListSize(page(RecurringExpensesPage.class).editButtons(), 2, 10);
                   UiActions.click(page(RecurringExpensesPage.class).editButtons().get(1));
                   UiActions.click(page(RecurringExpensesPage.class).confirmDelete());
                   continue;
                }
                else {
                    UiActions.click(page(RecurringExpensesPage.class).addExpense());
                    UiActions.enterText(page(IncomesPage.class).name(), name[i]);
                }           
                UiActions.enterText(page(RecurringExpensesPage.class).targetAmount(), targetAmount[i]);
                UiActions.enterText(page(RecurringExpensesPage.class).currentAmount(), currentAmount[i]);
            
                if (!liquidAmount[i].equals("Unchanged")) {
                    UiActions.enterText(page(RecurringExpensesPage.class).liquidAmount(), liquidAmount[i]);
                }
            
                String[] parts = years[i].split(" ", 2);
                String yearValue = parts[0];
                String risk = parts[1];
            
                UiActions.enterText(page(RecurringExpensesPage.class).years(), yearValue);
                
                int index = switch (risk) {
                    case "Without risk" -> 0;
                    case "Medium risk" -> 1;
                    case "High risk" -> 2;
                    default -> 0;
                };
            
                UiActions.click(page(RecurringExpensesPage.class).riskOptions().get(index));
                UiActions.click(testcaseName.equals("recurring expenses") ? page(IncomesPage.class).add() : page(FinancialGoalsPage.class).add());
                
                String text = UiActions.getText(page(RecurringExpensesPage.class).monthlyPayment());
                String months = text.replaceAll("\\D+", "");
                Verifications.verifyText(String.valueOf(Integer.parseInt(yearValue) * 12), months);
            }       
            UiActions.click(page(IncomesPage.class).nextButton());
            WaitForElement.waitUntilUrlContains("/FinancialPlanMaker/financialGoals");
        });
    }
    
    public static void financialGoals(String[] name, String[] targetAmount, String[] currentAmount,
    								  String[] liquidAmount, String[] years,String testcaseName) {
        additionExpenses(name, targetAmount, currentAmount, liquidAmount, years,testcaseName);
    }
    
    public static float getTotalExpense() {
        return state.get().totalExpense;
    }
}