package com.jim.pocketaccounter.debt;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.pocketaccounter.PocketAccounter;
import com.jim.pocketaccounter.PocketAccounterApplication;
import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.database.Account;
import com.jim.pocketaccounter.database.AccountDao;
import com.jim.pocketaccounter.database.DaoSession;
import com.jim.pocketaccounter.database.DebtBorrow;
import com.jim.pocketaccounter.database.DebtBorrowDao;
import com.jim.pocketaccounter.database.Recking;
import com.jim.pocketaccounter.managers.LogicManager;
import com.jim.pocketaccounter.managers.LogicManagerConstants;
import com.jim.pocketaccounter.managers.PAFragmentManager;
import com.jim.pocketaccounter.managers.ToolbarManager;
import com.jim.pocketaccounter.utils.DatePicker;
import com.jim.pocketaccounter.utils.OperationsListDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by user on 6/7/2016.
 */

public class InfoDebtBorrowFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    @Inject
    DatePicker datePicker;
    @Inject
    OperationsListDialog operationsListDialog;
    @Inject
    LogicManager logicManager;
    @Inject
    ToolbarManager toolbarManager;
    @Inject
    PAFragmentManager paFragmentManager;
    @Inject
    @Named(value = "display_formmatter")
    SimpleDateFormat dateFormat;
    @Inject
    DaoSession daoSession;
    DebtBorrowDao debtBorrowDao;
    AccountDao accountDao;

    private TextView borrowName;
    private TextView leftAmount;
    private TextView borrowLeftDate;
    private TextView totalPayAmount;
    private TextView calculate;
    private TextView tvTotalsummInfo;
    private FrameLayout borrowPay;
    private CircleImageView circleImageView;
    private android.support.v7.widget.RecyclerView recyclerView;
    private String id = "";
    private PeysAdapter peysAdapter;
    private Spinner accountSp;
    private DebtBorrow debtBorrow;
    private FrameLayout deleteFrame;
    private ImageView info;
    private FrameLayout infoFrame;
    private FrameLayout isHaveReking;
    private TextView tvInfoDebtBorrowTakeDate;
    private TextView payText;
    private boolean isCheks[];
    int mode = 1;
    private TextView phoneNumber;
    static int TYPE = 0;

    public static Fragment getInstance(String id, int type) {
        InfoDebtBorrowFragment fragment = new InfoDebtBorrowFragment();
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        bundle.putInt("type", type);
        TYPE = type;
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.info_debt_borrow_fragment_mod, container, false);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);
        debtBorrowDao = daoSession.getDebtBorrowDao();
        accountDao = daoSession.getAccountDao();
        borrowName = (TextView) view.findViewById(R.id.name_of_borrow);
        leftAmount = (TextView) view.findViewById(R.id.tvAmountDebtBorrowInfo);
        borrowLeftDate = (TextView) view.findViewById(R.id.tvLeftDayDebtBorrowInfo);
        borrowPay = (FrameLayout) view.findViewById(R.id.btPayDebtBorrowInfo);
        deleteFrame = (FrameLayout) view.findViewById(R.id.flInfoDebtBorrowDeleted);
        totalPayAmount = (TextView) view.findViewById(R.id.total_summ_debt_borrow);
        tvTotalsummInfo = (TextView) view.findViewById(R.id.tvInfoDebtBorrowTotalSumm);
        payText = (TextView) view.findViewById(R.id.paybut);
        phoneNumber = (TextView) view.findViewById(R.id.tvInfoDebtBorrowPhoneNumber);
        circleImageView = (CircleImageView) view.findViewById(R.id.iconDebtBorrowInfo);
        recyclerView = (RecyclerView) view.findViewById(R.id.rvDebtBorrowInfo);
        info = (ImageView) view.findViewById(R.id.ivInfoDebtBorrowInfo);
        infoFrame = (FrameLayout) view.findViewById(R.id.flInfoDebtBorrowVisibl);
        tvInfoDebtBorrowTakeDate = (TextView) view.findViewById(R.id.tvInfoDebtBorrowTakeDate);
        infoFrame.setVisibility(View.GONE);
        calculate = (TextView) view.findViewById(R.id.tvInfoDebtBorrowIsCalculate);
        id = getArguments().getString("id");
        isHaveReking = (FrameLayout) view.findViewById(R.id.ifListHave);

        debtBorrow = new DebtBorrow();
        if (debtBorrowDao.queryBuilder().list() != null) {
            for (DebtBorrow db : debtBorrowDao.queryBuilder().list()) {
                if (db.getId().matches(id)) {
                    debtBorrow = db;
                    break;
                }
            }
        }

        isCheks = new boolean[debtBorrow.getReckings().size()];
        for (int i = 0; i < isCheks.length; i++) {
            isCheks[i] = false;
        }
        toolbarManager.setImageToSecondImage(R.drawable.ic_delete_black);
        toolbarManager.setSpinnerVisibility(View.GONE);
        toolbarManager.setToolbarIconsVisibility(View.GONE, View.VISIBLE);
        if (!debtBorrow.getTo_archive()) {
            toolbarManager.setImageToSecondImage(R.drawable.ic_more_vert_black_48dp);
            toolbarManager.setOnSecondImageClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    final AlertDialog.Builder builderChouse = new AlertDialog.Builder(getActivity());
//                    builderChouse.setTitle("Choose type action").setItems(R.array.more_option_for_credit_debt, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            if (which == 0) {
//                                AddBorrowFragment temp = (AddBorrowFragment) AddBorrowFragment.getInstance(TYPE);
//                                temp.shareDetialDebtBorrow(debtBorrow);
//                                paFragmentManager.getFragmentManager().popBackStack();
//                                paFragmentManager.displayFragment(temp);
//                            } else {
//                                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//                                builder.setMessage(debtBorrow.getCalculate() ?
//                                        getResources().getString(R.string.delete_credit) : getString(R.string.delete))
//                                        .setPositiveButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
//                                            public void onClick(DialogInterface dialog, int id) {
//                                            }
//                                        }).setNegativeButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int id) {
//                                        dialog.cancel();
////                                        manager.getDebtBorrows().remove(debtBorrow);
////                                        manager.saveDebtBorrows();
////                                        manager.loadDebtBorrows();
////                                        ((PocketAccounter) getContext()).getSupportFragmentManager().popBackStack();
////                                        DebtBorrowFragment fragment = new DebtBorrowFragment();
////                                        Bundle bundle = new Bundle();
////                                        bundle.putInt("pos", debtBorrow.getTo_archive() ? 2 : debtBorrow.getType());
////                                        fragment.setArguments(bundle);
////                                        ((PocketAccounter) getContext()).replaceFragment(fragment, PockerTag.DEBTS);
//                                    }
//                                });
//                                builder.create().show();
//                            }
//
//
//                        }
//                    });
//                    builderChouse.create().show();
                    String[] st = new String[2];
                    st[0] = getResources().getString(R.string.edit);
                    st[1] = getResources().getString(R.string.delete);
                    operationsListDialog.setAdapter(st);
                    operationsListDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            if (position == 0) {
                                Fragment addBorrowFragment = AddBorrowFragment.getInstance(TYPE, debtBorrow);
                                paFragmentManager.displayFragment(addBorrowFragment);
                            } else {
                                switch (logicManager.deleteDebtBorrow(debtBorrow)) {
                                    case LogicManagerConstants.REQUESTED_OBJECT_NOT_FOUND: {
                                        Toast.makeText(getContext(), "No this debt", Toast.LENGTH_SHORT).show();
                                        operationsListDialog.dismiss();
                                        break;
                                    }
                                    case LogicManagerConstants.DELETED_SUCCESSFUL: {
                                        Toast.makeText(getContext(), "Success delete", Toast.LENGTH_SHORT).show();
                                        paFragmentManager.getFragmentManager().popBackStack();
                                        paFragmentManager.displayFragment(new DebtBorrowFragment());
                                        operationsListDialog.dismiss();
                                        break;
                                    }
                                }
                            }
                        }
                    });
                    operationsListDialog.show();
                }
            });
        } else {
            toolbarManager.setOnSecondImageClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage(debtBorrow.getCalculate() ?
                            getResources().getString(R.string.delete_credit) : getString(R.string.delete))
                            .setPositiveButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            }).setNegativeButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            switch (logicManager.deleteDebtBorrow(debtBorrow)) {
                                case LogicManagerConstants.REQUESTED_OBJECT_NOT_FOUND: {
                                    Toast.makeText(getContext(), "No has debt", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                                case LogicManagerConstants.DELETED_SUCCESSFUL: {
                                    paFragmentManager.getFragmentManager().popBackStack();
                                    DebtBorrowFragment fragment = new DebtBorrowFragment();
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("pos", debtBorrow.getTo_archive() ? 2 : debtBorrow.getType());
                                    fragment.setArguments(bundle);
                                    paFragmentManager.displayFragment(fragment);
                                    Toast.makeText(getContext(), "Delete success", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }
                        }
                    });
                    builder.create().show();
                }
            });
        }
//        phoneNumber.setText(debtBorrow.getPerson().getPhoneNumber());
        tvInfoDebtBorrowTakeDate.setText(dateFormat.format(debtBorrow.getTakenDate().getTime()));
        calculate.setText(debtBorrow.getCalculate() ?
                getString(R.string.calc_in_balance) :
                getString(R.string.no_calc_in_balance));
        infoFrame.setVisibility(View.GONE);
        view.findViewById(R.id.with_wlyuzik).setVisibility(view.GONE);
        view.findViewById(R.id.infoooc).setOnClickListener(new View.OnClickListener() {
                                                               @Override
                                                               public void onClick(View v) {
                                                                   if (infoFrame.getVisibility() == View.GONE) {
                                                                       infoFrame.setVisibility(View.VISIBLE);
                                                                       info.setImageResource(R.drawable.pasga_ochil);
                                                                       view.findViewById(R.id.with_wlyuzik).setVisibility(View.VISIBLE);
                                                                   } else {
                                                                       infoFrame.setVisibility(View.GONE);
                                                                       info.setImageResource(R.drawable.infoo);
                                                                       view.findViewById(R.id.with_wlyuzik).setVisibility(view.GONE);
                                                                   }
                                                               }
                                                           }
        );
        if (debtBorrow.getReckings().isEmpty()) {
            isHaveReking.setVisibility(View.GONE);
        }

        //        peysAdapter = new PeysAdapter(debtBorrow.getReckings());
        Calendar currentDate = Calendar.getInstance();
        int day = 0;
        int mounth = 0;
        int year = 0;

        if (debtBorrow.getReturnDate() != null && currentDate.compareTo(debtBorrow.getReturnDate()) <= 0) {
            if (currentDate.get(Calendar.DAY_OF_MONTH) <= debtBorrow.getReturnDate().get(Calendar.DAY_OF_MONTH)) {
                day = debtBorrow.getReturnDate().get(Calendar.DAY_OF_MONTH) - currentDate.get(Calendar.DAY_OF_MONTH);
                if (currentDate.get(Calendar.MONTH) <= debtBorrow.getReturnDate().get(Calendar.MONTH)) {
                    mounth = debtBorrow.getReturnDate().get(Calendar.MONTH) - currentDate.get(Calendar.MONTH);
                    if (currentDate.get(Calendar.YEAR) <= debtBorrow.getReturnDate().get(Calendar.YEAR)) {
                        year = debtBorrow.getReturnDate().get(Calendar.YEAR) - currentDate.get(Calendar.YEAR);
                    }
                } else {
                    mounth = debtBorrow.getReturnDate().get(Calendar.MONTH) + 12 - currentDate.get(Calendar.MONTH);
                    year = debtBorrow.getReturnDate().get(Calendar.YEAR) - 1 - currentDate.get(Calendar.YEAR);
                }
            } else {
                debtBorrow.getReturnDate().add(Calendar.MONTH, -1);
                day = debtBorrow.getReturnDate().get(Calendar.DAY_OF_MONTH) + debtBorrow.getReturnDate().getActualMaximum(Calendar.MONTH) - currentDate.get(Calendar.DAY_OF_MONTH);
                if (debtBorrow.getReturnDate().get(Calendar.MONTH) >= currentDate.get(Calendar.MONTH)) {
                    mounth = debtBorrow.getReturnDate().get(Calendar.MONTH) - currentDate.get(Calendar.MONTH);
                    if (debtBorrow.getReturnDate().get(Calendar.YEAR) >= currentDate.get(Calendar.YEAR)) {
                        year = debtBorrow.getReturnDate().get(Calendar.YEAR) - currentDate.get(Calendar.YEAR);
                    }
                } else {
                    mounth = debtBorrow.getReturnDate().get(Calendar.MONTH) + 12 - currentDate.get(Calendar.MONTH);
                    if (debtBorrow.getReturnDate().get(Calendar.YEAR) > currentDate.get(Calendar.YEAR)) {
                        year = debtBorrow.getReturnDate().get(Calendar.YEAR) - 1 - currentDate.get(Calendar.YEAR);
                    }
                }
            }
        }

        List<Recking> list = debtBorrow.getReckings();
        double total = 0;
        for (Recking rc : list) {
            total += rc.getAmount();
        }
        if (debtBorrow.getTo_archive()) {
            deleteFrame.setVisibility(View.GONE);
            borrowPay.setVisibility(View.GONE);
        }
//        borrowName.setText(debtBorrow.getPerson().getName());
        String qq = ((int) (debtBorrow.getAmount() - total)) == (debtBorrow.getAmount() - total)
                ? "" + ((int) (debtBorrow.getAmount() - total)) : "" + (debtBorrow.getAmount() - total);
        if (total >= debtBorrow.getAmount())
            leftAmount.setText(getResources().getString(R.string.repaid));
        else
            leftAmount.setText(qq + debtBorrow.getCurrency().getAbbr());
        String sana = (year != 0 ? year + " " + getString(R.string.year) : "")
                + (mounth != 0 ? mounth + " " + getString(R.string.moth) : "")
                + (day != 0 ? day + " " + getString(R.string.day) : "");
        if (debtBorrow.getReturnDate() == null) {
            borrowLeftDate.setText(getResources().getString(R.string.no_date_selected));
        } else {
            borrowLeftDate.setText(sana);
        }
        totalPayAmount.setText("" + (total == ((int) total) ? ("" + ((int) total)) : ("" + total)) + debtBorrow.getCurrency().getAbbr());
        if (total >= debtBorrow.getAmount()) {
            payText.setText(getResources().getString(R.string.archive));
            deleteFrame.setVisibility(View.GONE);
        }

//        if (!debtBorrow.getPerson().getPhoto().equals("") && !debtBorrow.getPerson().getPhoto().matches("0")) {
//            try {
//                circleImageView.setImageBitmap(queryContactImage(Integer.parseInt(debtBorrow.getPerson().getPhoto())));
//            } catch (NumberFormatException e) {
//                circleImageView.setImageDrawable(Drawable.createFromPath(debtBorrow.getPerson().getPhoto()));
//            }
//        } else {
//            circleImageView.setImageResource(R.drawable.no_photo);
//        }
        tvTotalsummInfo.setText("" + (debtBorrow.getAmount() == ((int) debtBorrow.getAmount())
                ? ("" + (int) debtBorrow.getAmount()) : ("" + debtBorrow.getAmount())) + debtBorrow.getCurrency().
                getAbbr());
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(peysAdapter);
        borrowPay.setOnClickListener(this);
        deleteFrame.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View view) {
                                               if (mode == 1) mode = 0;
                                               else mode = 1;
                                               if (mode == 0) {
                                                   for (int i = 0; i < peysAdapter.getItemCount(); i++) {
                                                       peysAdapter.notifyItemChanged(i);
                                                   }
                                                   payText.setText(getResources().getString(R.string.cancel));
                                               } else {
                                                   boolean tek = false;
                                                   for (boolean isChek : isCheks) {
                                                       if (isChek) {
                                                           tek = true;
                                                       }
                                                   }
                                                   if (tek) {
                                                       final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                       builder.setMessage(getString(R.string.delete_question))
                                                               .setPositiveButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                                                   public void onClick(DialogInterface d, int id) {
                                                                       mode = 0;
                                                                       d.dismiss();
                                                                   }
                                                               }).setNegativeButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                           public void onClick(DialogInterface d, int id) {
                                                               for (int i = isCheks.length - 1; i >= 0; i--) {
                                                                   if (isCheks[i]) {
                                                                       peysAdapter.itemDeleted(i);
                                                                   } else {
                                                                       peysAdapter.notifyItemChanged(i);
                                                                   }
                                                               }
                                                               isCheks = new boolean[debtBorrow.getReckings().size()];
                                                               for (int i = 0; i < isCheks.length; i++) {
                                                                   isCheks[i] = false;
                                                               }
                                                               payText.setText(getResources().getString(R.string.payy));
                                                               d.cancel();
                                                           }
                                                       });
                                                       builder.create().show();
                                                   } else {
                                                       mode = 1;
                                                       for (int i = 0; i < isCheks.length; i++) {
                                                           isCheks[i] = false;
                                                           peysAdapter.notifyItemChanged(i);
                                                       }
                                                       payText.setText(getResources().getString(R.string.payy));
                                                   }
                                               }
                                           }
                                       }
        );
        return view;
    }

    private Bitmap queryContactImage(int imageDataRow) {
        Cursor c = getContext().getContentResolver().query(ContactsContract.Data.CONTENT_URI, new String[]{
                ContactsContract.CommonDataKinds.Photo.PHOTO
        }, ContactsContract.Data._ID + "=?", new String[]{
                Integer.toString(imageDataRow)
        }, null);
        byte[] imageBytes = null;
        if (c != null) {
            if (c.moveToFirst()) {
                imageBytes = c.getBlob(0);
            }
            c.close();
        }
        if (imageBytes != null) {
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        } else {
            return null;
        }
    }

    private boolean isMumkin(DebtBorrow debt, String accountId, Double summ) {
        Account account = null;
//        for (Account ac : manager.getAccounts()) {
//            if (ac.getId().matches(accountId)) {
//                account = ac;
//                break;
//            }
//        }
//        if (account.isLimited() && debt.getCalculate()) {
//            double limit = account.getLimitSum();
//            double accounted = PocketAccounterGeneral.getCost(debt.getTakenDate(),account.getStartMoneyCurrency(),account.getLimitCurrency(),account.getAmount());
//            for (int i = 0; i < PocketAccounter.financeManager.getRecords().size(); i++) {
//                FinanceRecord tempac=PocketAccounter.financeManager.getRecords().get(i);
//                if (tempac.getAccount().getId().matches(account.getId())) {
//                    if (tempac.getCategory().getType() == PocketAccounterGeneral.INCOME)
//                        accounted = accounted + PocketAccounterGeneral.getCost(tempac.getDate(),tempac.getCurrency(),account.getLimitCurrency(),tempac.getAmount());
//                    else
//                        accounted = accounted - PocketAccounterGeneral.getCost(tempac.getDate(),tempac.getCurrency(),account.getLimitCurrency(),tempac.getAmount());
//                }
//            }
//            for (DebtBorrow debtBorrow : PocketAccounter.financeManager.getDebtBorrows()) {
//                if (debtBorrow.getCalculate()) {
//                    if (debtBorrow.getAccount().getId().matches(account.getId())) {
//                        if (debtBorrow.getType() == DebtBorrow.BORROW) {
//                            accounted = accounted - PocketAccounterGeneral.getCost(debtBorrow.getTakenDate(), debtBorrow.getCurrency(),account.getLimitCurrency(), debtBorrow.getAmount());
//                        } else {
//                            accounted = accounted + PocketAccounterGeneral.getCost(debtBorrow.getTakenDate(), debtBorrow.getCurrency(),account.getLimitCurrency(), debtBorrow.getAmount());
//                        }
//                        for (Recking recking : debtBorrow.getReckings()) {
//                            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
//                            Calendar cal = Calendar.getInstance();
//                            try {
//                                cal.setTime(format.parse(recking.getPayDate()));
//                            } catch (ParseException e) {
//                                e.printStackTrace();
//                            }
//                            if (debtBorrow.getType() == DebtBorrow.DEBT) {
//                                accounted = accounted - PocketAccounterGeneral.getCost(cal, debtBorrow.getCurrency(),account.getLimitCurrency(), recking.getAmount());
//                            } else {
//                                accounted = accounted + PocketAccounterGeneral.getCost(cal, debtBorrow.getCurrency(),account.getLimitCurrency(), recking.getAmount());
//                            }
//                        }
//                    }else {
//                        for (Recking recking : debtBorrow.getReckings()) {
//                            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
//                            Calendar cal = Calendar.getInstance();
//                            if (recking.getAccountId().matches(account.getId())) {
//                                try {
//                                    cal.setTime(format.parse(recking.getPayDate()));
//                                } catch (ParseException e) {
//                                    e.printStackTrace();
//                                }
//                                if (debtBorrow.getType() == DebtBorrow.BORROW) {
//                                    accounted = accounted + PocketAccounterGeneral.getCost(cal, debtBorrow.getCurrency(),account.getLimitCurrency(), recking.getAmount());
//                                } else {
//                                    accounted = accounted - PocketAccounterGeneral.getCost(cal, debtBorrow.getCurrency(),account.getLimitCurrency(), recking.getAmount());
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//            for (CreditDetials creditDetials : PocketAccounter.financeManager.getCredits()) {
//                if (creditDetials.isKey_for_include()) {
//                    for (ReckingCredit reckingCredit : creditDetials.getReckings()) {
//                        if (reckingCredit.getAccountId().matches(account.getId())) {
//                            Calendar cal = Calendar.getInstance();
//                            cal.setTimeInMillis(reckingCredit.getPayDate());
//                            accounted = accounted - PocketAccounterGeneral.getCost(cal, creditDetials.getValyute_currency(),account.getLimitCurrency(), reckingCredit.getAmount());
//                        }
//                    }
//                }
//            }
//            if (debt.getType() == DebtBorrow.DEBT) {
//                accounted = accounted - PocketAccounterGeneral.getCost(Calendar.getInstance(), debt.getCurrency(),account.getLimitCurrency(), summ);
//            } else {
//                accounted = accounted + PocketAccounterGeneral.getCost(Calendar.getInstance(), debt.getCurrency(),account.getLimitCurrency(), summ);
//            }
//
//            if (-limit > accounted) {
//                Toast.makeText(getContext(), R.string.limit_exceed, Toast.LENGTH_SHORT).show();
//                return false;
//            }
//            return true;
//        }
        return true;
    }

    boolean tek = false;

    private void openDialog() {
        if (!payText.getText().toString().matches(getResources().getString(R.string.archive))) {
            final Dialog dialog = new Dialog(getActivity());
            View dialogView = getActivity().getLayoutInflater().inflate(R.layout.add_pay_debt_borrow_info_mod, null);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(dialogView);
            final EditText enterDate = (EditText) dialogView.findViewById(R.id.etInfoDebtBorrowDate);
            final EditText enterPay = (EditText) dialogView.findViewById(R.id.etInfoDebtBorrowPaySumm);
            final EditText comment = (EditText) dialogView.findViewById(R.id.etInfoDebtBorrowPayComment);
            accountSp = (Spinner) dialogView.findViewById(R.id.spInfoDebtBorrowAccount);

            final String[] accaounts = new String[accountDao.queryBuilder().list().size()];
            for (int i = 0; i < accaounts.length; i++) {
                accaounts[i] = accountDao.queryBuilder().list().get(i).getName();
            }

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                    getContext(), R.layout.spiner_gravity_right, accaounts);

            accountSp.setAdapter(arrayAdapter);

            if (!debtBorrow.getCalculate()) {
                dialogView.findViewById(R.id.is_calc).setVisibility(View.GONE);
            }

            ImageView cancel = (ImageView) dialogView.findViewById(R.id.ivInfoDebtBorrowCancel);
            ImageView save = (ImageView) dialogView.findViewById(R.id.ivInfoDebtBorrowSave);
            final Calendar date = Calendar.getInstance();
            enterDate.setText(dateFormat.format(date.getTime()));
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            final DatePickerDialog.OnDateSetListener getDatesetListener = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(android.widget.DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    date.set(year, monthOfYear, dayOfMonth);
                    if (date.compareTo(debtBorrow.getTakenDate()) < 0) {
                        date.setTime(debtBorrow.getTakenDate().getTime());
                        enterDate.setError(getResources().getString(R.string.incorrect_date));
                    } else enterDate.setError(null);
                    enterDate.setText(dateFormat.format(date.getTime()));
                }
            };
            enterDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Calendar calendar = Calendar.getInstance();
                    Dialog mDialog = new DatePickerDialog(getContext(),
                            getDatesetListener, calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH), calendar
                            .get(Calendar.DAY_OF_MONTH));
                    mDialog.show();
                }
            });
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tek = true;
                    int len = debtBorrow.getCurrency().getAbbr().length();
                    if (!enterPay.getText().toString().isEmpty() && Double.parseDouble(enterPay.getText().toString()) != 0) {
//                        if (debtBorrow.getCalculate() && isMumkin(debtBorrow, manager.getAccounts().get(accountSp.getSelectedItemPosition()).getId(), Double.parseDouble(enterPay.getText().toString())))
//                            tek = true;
//                        if (!debtBorrow.getCalculate()) tek = true;

                        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        if (Double.parseDouble(leftAmount.getText().toString().substring(0, leftAmount.getText().toString().length() - len))
                                - Double.parseDouble(enterPay.getText().toString()) < 0) {
                            builder.setMessage(getResources().getString(R.string.incorrect_pay))
                                    .setPositiveButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                        }
                                    }).setNegativeButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    d.cancel();
                                    if (tek) {
                                        peysAdapter.setDataChanged(dateFormat.format(date.getTime()), Double.parseDouble(enterPay.getText().toString()),
                                                "" + accountSp.getSelectedItem(), comment.getText().toString());
                                        dialog.dismiss();
                                    }
                                }
                            });
                            if (tek) {
                                builder.create().show();
                            }
                        } else {
                            if (tek) {
                                peysAdapter.setDataChanged(dateFormat.format(date.getTime()), Double.parseDouble(enterPay.getText().toString()),
                                        "" + accountSp.getSelectedItem(), comment.getText().toString());
                                dialog.dismiss();
                            }
                        }
                    } else {
                        enterPay.setError(getResources().getString(R.string.enter_pay_value));
                    }
                }
            });
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int width = displayMetrics.widthPixels;
            dialog.getWindow().setLayout(7 * width / 8, RelativeLayout.LayoutParams.WRAP_CONTENT);
            dialog.show();
        } else {
//            for (int i = 0; i < manager.getDebtBorrows().size(); i++) {
//                if (manager.getDebtBorrows().get(i).getId().matches(debtBorrow.getId())) {
//                    manager.getDebtBorrows().get(i).setTo_archive(true);
//                    break;
//                }
//            }

//            ((PocketAccounter) getContext()).getSupportFragmentManager().popBackStack();
//            DebtBorrowFragment fragment = new DebtBorrowFragment();
//            Bundle bundle = new Bundle();
//            bundle.putInt("pos", debtBorrow.getType());
//            fragment.setArguments(bundle);
//            ((PocketAccounter) getContext()).replaceFragment(fragment, PockerTag.DEBTS);
        }
    }

    @Override
    public void onClick(View v) {
        if (payText.getText().toString().matches(getResources().getString(R.string.cancel))) {
            mode = 1;
            for (int i = 0; i < isCheks.length; i++) {
                isCheks[i] = false;
                peysAdapter.notifyItemChanged(i);
            }
            payText.setText(getResources().getString(R.string.payy));
        } else {
            openDialog();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private class PeysAdapter extends RecyclerView.Adapter<InfoDebtBorrowFragment.ViewHolder> {
        private ArrayList<Recking> list;

        public PeysAdapter(ArrayList<Recking> list) {
            this.list = list;
        }

        public int getItemCount() {
            return list.size();
        }

        public void onBindViewHolder(final InfoDebtBorrowFragment.ViewHolder view, final int position) {
            view.infoDate.setText(getString(R.string.pay_daet) + list.get(position).getPayDate());
            view.infoSumm.setText((list.get(position).getAmount() == ((int) list.get(position).getAmount())
                    ? ("" + ((int) list.get(position).getAmount())) : ("" + list.get(position).getAmount()))
                    + "" + debtBorrow.getCurrency().getAbbr());
            if (!debtBorrow.getCalculate()) {
                view.infoAccount.setVisibility(View.GONE);
            } else {
                for (Account account : accountDao.queryBuilder().list()) {
                    if (account.getId().matches(list.get(position).getAccountId())) {
                        view.infoAccount.setText(getString(R.string.by) + account.getName());
                        break;
                    }
                }
            }
            if (mode == 0) {
                view.rootView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        view.checkBox.setChecked(!view.checkBox.isChecked());
                        isCheks[position] = !isCheks[position];
                    }
                });
            }
            if (!list.get(position).getComment().matches("")) {
                view.comment.setText(getResources().getString(R.string.comment) + ": " + list.get(position).getComment());
            } else {
                view.comment.setVisibility(View.GONE);
            }
            if (mode == 1) {
                view.checkBox.setVisibility(View.GONE);
            } else {
                view.checkBox.setVisibility(View.VISIBLE);
            }
            view.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    view.checkBox.setChecked(isCheks[position]);
                    if (view.checkBox.isChecked()) {
                        view.checkBox.setChecked(false);
                    } else {
                        view.checkBox.setChecked(true);
                    }
                    isCheks[position] = view.checkBox.isChecked();
                }
            });
            view.checkBox.setChecked(isCheks[position]);
        }

        public InfoDebtBorrowFragment.ViewHolder onCreateViewHolder(ViewGroup parent, int var2) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.payed_item, parent, false);
            return new ViewHolder(view);
        }

        public void itemDeleted(int position) {
            list.remove(position);
            notifyItemRemoved(position);
            double total = 0;
            for (Recking rc : list) {
                total += rc.getAmount();
            }
            totalPayAmount.setText((total == ((int) total) ? ("" + ((int) total)) : ("" + total)) + debtBorrow.getCurrency().getAbbr());
            String qq = ((int) (debtBorrow.getAmount() - total)) == (debtBorrow.getAmount() - total)
                    ? "" + ((int) (debtBorrow.getAmount() - total)) : "" + (debtBorrow.getAmount() - total);
            leftAmount.setText(qq + "" + debtBorrow.getCurrency().getAbbr());
            if (debtBorrow.getReckings().isEmpty()) {
                isHaveReking.setVisibility(View.GONE);
            }
        }

        public void setDataChanged(String clDate, double value, String accountId, String comment) {
            for (Account account : accountDao.queryBuilder().list()) {
                if (account.getName().matches(accountId)) {
                    accountId = account.getId();
                    break;
                }
            }
            Recking recking = new Recking(clDate, value, debtBorrow.getId(), accountId, comment);
            list.add(0, recking);
            double qoldiq = 0;
            for (int i = 0; i < list.size(); i++) {
                qoldiq += list.get(i).getAmount();
            }
            String qq = ((int) (debtBorrow.getAmount() - qoldiq)) == (debtBorrow.getAmount() - qoldiq)
                    ? ("" + ((int) (debtBorrow.getAmount() - qoldiq))) : ("" + (debtBorrow.getAmount() - qoldiq));

            leftAmount.setText(qq + "" + debtBorrow.getCurrency().getAbbr());
            totalPayAmount.setText("" + (qoldiq == ((int) qoldiq) ? ("" + ((int) qoldiq)) : ("" + qoldiq)) + "" + debtBorrow.getCurrency().getAbbr());

            if (qoldiq >= debtBorrow.getAmount()) {
                payText.setText(getResources().getString(R.string.archive));
                deleteFrame.setVisibility(View.GONE);
                leftAmount.setText(getResources().getString(R.string.repaid));
            }

            debtBorrow.getReckings().addAll(list);
            isHaveReking.setVisibility(View.VISIBLE);
            notifyItemInserted(0);
            isCheks = new boolean[list.size()];
            for (int i = 0; i < isCheks.length; i++) {
                isCheks[i] = false;
            }
        }
    }

    public class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
        public TextView infoDate;
        public TextView infoSumm;
        public TextView infoAccount;
        public TextView comment;
        public CheckBox checkBox;
        public View rootView;

        public ViewHolder(View view) {
            super(view);
            infoDate = (TextView) view.findViewById(R.id.date_of_trans);
            infoAccount = (TextView) view.findViewById(R.id.via_acount);
            comment = (TextView) view.findViewById(R.id.comment_trans);
            infoSumm = (TextView) view.findViewById(R.id.paid_value);
            checkBox = (CheckBox) view.findViewById(R.id.for_delete_check_box);
            rootView = view.findViewById(R.id.rlRootView);
        }
    }
}