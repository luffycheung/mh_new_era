package com.jim.finansia.modulesandcomponents.components;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.credit.AdapterCridet;
import com.jim.finansia.credit.AdapterCridetArchive;
import com.jim.finansia.credit.notificat.AutoMarketService;
import com.jim.finansia.credit.notificat.NotificationManagerCredit;
import com.jim.finansia.debt.AddBorrowFragment;
import com.jim.finansia.debt.BorrowFragment;
import com.jim.finansia.debt.DebtBorrowFragment;
import com.jim.finansia.debt.InfoDebtBorrowFragment;
import com.jim.finansia.finance.CurrencyAdapter;
import com.jim.finansia.finance.CurrencyExchangeAdapter;
import com.jim.finansia.fragments.AccountEditFragment;
import com.jim.finansia.fragments.AccountFragment;
import com.jim.finansia.fragments.AccountInfoFragment;
import com.jim.finansia.fragments.AddAutoMarketFragment;
import com.jim.finansia.fragments.AddCreditFragment;
import com.jim.finansia.fragments.AddSmsParseFragment;
import com.jim.finansia.fragments.AutoMarketFragment;
import com.jim.finansia.fragments.CategoryFragment;
import com.jim.finansia.fragments.CategoryInfoFragment;
import com.jim.finansia.fragments.ChangeColorOfStyleFragment;
import com.jim.finansia.fragments.CreditArchiveFragment;
import com.jim.finansia.fragments.CreditFragment;
import com.jim.finansia.fragments.CreditTabLay;
import com.jim.finansia.fragments.CurrencyChooseFragment;
import com.jim.finansia.fragments.CurrencyEditFragment;
import com.jim.finansia.fragments.CurrencyFragment;
import com.jim.finansia.fragments.DetailedCreditsFragment;
import com.jim.finansia.fragments.DetailedDebtBorrowsFragment;
import com.jim.finansia.fragments.DetailedSmsSuccessesFragment;
import com.jim.finansia.fragments.FinanceRecordsFragment;
import com.jim.finansia.fragments.InfoCreditFragment;
import com.jim.finansia.fragments.InfoCreditFragmentForArchive;
import com.jim.finansia.fragments.MainFragment;
import com.jim.finansia.fragments.MainPageFragment;
import com.jim.finansia.fragments.ManualEnterFragment;
import com.jim.finansia.fragments.PABaseFragment;
import com.jim.finansia.fragments.PurposeEditFragment;
import com.jim.finansia.fragments.PurposeFragment;
import com.jim.finansia.fragments.PurposeInfoFragment;
import com.jim.finansia.fragments.RecordDetailFragment;
import com.jim.finansia.fragments.RecordEditFragment;
import com.jim.finansia.fragments.ReportByAccountFragment;
import com.jim.finansia.fragments.ReportByCategory;
import com.jim.finansia.fragments.ReportByCategoryExpansesFragment;
import com.jim.finansia.fragments.ReportByCategoryIncomesFragment;
import com.jim.finansia.fragments.ReportByCategoryRootCategoryFragment;
import com.jim.finansia.fragments.ReportByIncomeAndExpense;
import com.jim.finansia.fragments.ReportByIncomeExpanseBarFragment;
import com.jim.finansia.fragments.ReportByIncomeExpanseTableFragment;
import com.jim.finansia.fragments.ReportByIncomeExpenseDaily;
import com.jim.finansia.fragments.ReportByIncomeExpenseDailyTableFragment;
import com.jim.finansia.fragments.ReportByIncomeExpenseMonthlyFragment;
import com.jim.finansia.fragments.ReportByCategoryFragment;
import com.jim.finansia.fragments.ReportFragment;
import com.jim.finansia.fragments.RootCategoryEditFragment;
import com.jim.finansia.fragments.SMSParseInfoFragment;
import com.jim.finansia.fragments.SearchFragment;
import com.jim.finansia.fragments.SmsParseMainFragment;
import com.jim.finansia.fragments.TableBarFragment;
import com.jim.finansia.fragments.VoiceRecognizerFragment;
import com.jim.finansia.managers.SettingsManager;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.modulesandcomponents.modules.PocketAccounterActivityModule;
import com.jim.finansia.report.BarReportView;
import com.jim.finansia.report.CategoryReportView;
import com.jim.finansia.report.ReportByCategoryDialogAdapter;
import com.jim.finansia.report.ReportByIncomeExpanseDialogAdapter;
import com.jim.finansia.report.TableView;
import com.jim.finansia.utils.SubCatAddEditDialog;
import com.jim.finansia.utils.TransferAddEditDialog;
import com.jim.finansia.utils.TransferDialog;
import com.jim.finansia.utils.billing.MainPageLockView;
import com.jim.finansia.utils.billing.PurchaseImplementation;
import com.jim.finansia.utils.record.BaseBoardView;
import com.jim.finansia.utils.reportviews.CategorySliding;
import com.jim.finansia.utils.reportviews.MonthPickSliderView;
import com.jim.finansia.utils.reportviews.ReportSelectingYearWithMonthsView;

import dagger.Component;

/**
 * Created by DEV on 27.08.2016.
 */
@Component(
        modules = {PocketAccounterActivityModule.class},
        dependencies = {PocketAccounterApplicationComponent.class}
)
public interface PocketAccounterActivityComponent {
    void inject(PocketAccounter pocketAccounter);
    void inject(SettingsManager settingsManager);
    void inject(CurrencyFragment currencyFragment);
    void inject(CurrencyAdapter currencyAdapter);
    void inject(CurrencyChooseFragment currencyChooseFragment);
    void inject(CurrencyEditFragment currencyEditFragment);
    void inject(CurrencyExchangeAdapter currencyExchangeAdapter);
    void inject(AccountFragment accountFragment);
    void inject(AccountEditFragment accountEditFragment);
    void inject(CategoryFragment categoryFragment);
    void inject(SubCatAddEditDialog subCatAddEditDialog);
    void inject(RootCategoryEditFragment rootCategoryEditFragment);
    void inject(CategoryInfoFragment categoryInfoFragment);
    void inject(PurposeFragment purposeFragment);
    void inject(TransferDialog transferDialog);
    void inject(PurposeEditFragment purposeEditFragment);
    void inject(PurposeInfoFragment purposeInfoFragment);
    void inject(DebtBorrowFragment debtBorrowFragment);
    void inject(InfoDebtBorrowFragment infoDebtBorrowFragment);
    void inject(BorrowFragment borrowFragment);
    void inject(AddBorrowFragment addBorrowFragment);
    void inject(AdapterCridetArchive adapterCridetArchive);
    void inject(AdapterCridet adapterCridet);
    void inject(CreditTabLay creditTabLay);
    void inject(CreditFragment creditFragment);
    void inject(InfoCreditFragment infoCreditFragment);
    void inject(InfoCreditFragmentForArchive infoCreditFragmentForArchive);
    void inject(AddCreditFragment addCreditFragment);
    void inject(NotificationManagerCredit notificationManagerCredit);
    void inject(ToolbarManager toolbarManager);
    void inject(RecordEditFragment recordEditFragment);
    void inject(RecordDetailFragment recordDetailFragment);
    void inject(MainPageFragment mainPageFragment);
    void inject(AutoMarketFragment autoMarketFragment);
    void inject(AddAutoMarketFragment addAutoMarketFragment);
    void inject(SearchFragment searchFragment);
    void inject(AutoMarketService autoMarketService);
    void inject(ReportByAccountFragment reportByAccountFragment);
    void inject(ReportByCategory reportByCategory);
    void inject(ReportByCategoryExpansesFragment reportByCategoryExpansesFragment);
    void inject(ReportByCategoryIncomesFragment reportByCategoryIncomesFragment);
    void inject(CategoryReportView categoryReportView);
    void inject(SMSParseInfoFragment smsParseEditFragment);
    void inject(ReportByCategoryDialogAdapter reportByCategoryDialogAdapter);
    void inject(SmsParseMainFragment smsParseMainFragment);
    void inject(TableBarFragment tableBarFragment);
    void inject(ReportByIncomeExpanseTableFragment reportByIncomeExpanseTableFragment);
    void inject(TableView tableView);
    void inject(ReportByIncomeExpanseDialogAdapter reportByIncomeExpanseDialogAdapter);
    void inject(BarReportView barReportView);
    void inject(ReportByIncomeExpanseBarFragment reportByIncomeExpanseBarFragment);
    void inject(AddSmsParseFragment addSmsParseFragment);
    void inject(TransferAddEditDialog transferAddEditDialog);
    void inject(CreditArchiveFragment creditArchiveFragment);
    void inject(PABaseFragment paBaseFragment);
    void inject(BaseBoardView baseBoardView);
    void inject(MainPageLockView mainPageLockView);
    void inject(VoiceRecognizerFragment voiceRecognizerFragment);
    void inject(PurchaseImplementation purchaseImplementation);
    void inject(ReportByCategoryFragment reportFragment);
    void inject(ReportByCategoryRootCategoryFragment reportByCategoryRootCategoryFragment);
    void inject(CategorySliding categorySliding);
    void inject(ReportByIncomeAndExpense reportByIncomeAndExpense);
    void inject(ReportByIncomeExpenseDaily reportByIncomeExpenseDaily);
    void inject(MonthPickSliderView monthPickSliderView);
    void inject(ReportByIncomeExpenseMonthlyFragment reportByIncomeExpenseMonthlyFragment);
    void inject(ReportFragment reportFragment);
    void inject(ChangeColorOfStyleFragment changeColorOfStyleFragment);
    void inject(ReportSelectingYearWithMonthsView reportSelectingYearWithMonthsView);
    void inject(ReportByIncomeExpenseDailyTableFragment reportByIncomeExpenseDailyTableFragment);
    void inject(ManualEnterFragment manualEnterFragment);
    void inject(FinanceRecordsFragment financeRecordsFragment);
    void inject(DetailedCreditsFragment detailedCreditsFragment);
    void inject(DetailedDebtBorrowsFragment detailedDebtBorrowsFragment);
    void inject(DetailedSmsSuccessesFragment detailedSmsSuccessesFragment);
    void inject(MainFragment mainFragment);
}
