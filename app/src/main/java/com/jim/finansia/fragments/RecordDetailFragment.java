package com.jim.finansia.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jim.finansia.PocketAccounter;
import com.jim.finansia.PocketAccounterApplication;
import com.jim.finansia.R;
import com.jim.finansia.database.DaoSession;
import com.jim.finansia.database.FinanceRecord;
import com.jim.finansia.database.PhotoDetails;
import com.jim.finansia.managers.LogicManager;
import com.jim.finansia.managers.PAFragmentManager;
import com.jim.finansia.managers.ToolbarManager;
import com.jim.finansia.photocalc.PhotoAdapter;
import com.jim.finansia.utils.PocketAccounterGeneral;
import com.jim.finansia.utils.WarningDialog;
import com.jim.finansia.utils.cache.DataCache;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

@SuppressLint("ValidFragment")
public class RecordDetailFragment extends Fragment implements OnClickListener {
    private Calendar date;
    private RecyclerView rvRecordDetail;
    private int mode = PocketAccounterGeneral.NORMAL_MODE;
    private ArrayList<FinanceRecord> records;
    private boolean[] selections;
    private boolean onNoPressed = false;
    Context context;
    @Inject
    DaoSession daoSession;
    @Inject
    ToolbarManager toolbarManager;
    @Inject
    PAFragmentManager paFragmentManager;
    @Inject
    LogicManager logicManager;
    @Inject
    DataCache dataCache;

    @SuppressLint("ValidFragment")
    public RecordDetailFragment(Calendar date) {
        this.date = (Calendar) date.clone();
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.record_detail_layout, container, false);
        ((PocketAccounter) getContext()).component((PocketAccounterApplication) getContext().getApplicationContext()).inject(this);

        rvRecordDetail = (RecyclerView) rootView.findViewById(R.id.rvRecordDetail);
        refreshList();
        setMode(mode);
        return rootView;
    }
    public void onResume() {
        super.onResume();
        if (toolbarManager != null)
        {
            toolbarManager.setToolbarIconsVisibility(View.GONE, View.GONE, View.VISIBLE);
            toolbarManager.setImageToSecondImage(R.drawable.pencil);
            toolbarManager.setOnSecondImageClickListener(this);
            toolbarManager.setImageToHomeButton(R.drawable.ic_back_button);
            toolbarManager.setOnHomeButtonClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int size = 0;
                    size = paFragmentManager.getFragmentManager().getBackStackEntryCount();
                    for (int i = 0; i < size; i++) {
                        ((PocketAccounter)getContext()).getSupportFragmentManager().popBackStack();
                    }
                    paFragmentManager.displayMainWindow();
                }
            });
            toolbarManager.setTitle(getResources().getString(R.string.records));
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd,LLL yyyy");
            toolbarManager.setSubtitle(dateFormat.format(date.getTime()));
        }
    }

    private void refreshList() {
        records = new ArrayList<>();
        List<FinanceRecord> allrecords = daoSession.getFinanceRecordDao().loadAll();
        int size = allrecords.size();

        Calendar begin = (Calendar) date.clone();
        begin.set(Calendar.HOUR_OF_DAY, 0);
        begin.set(Calendar.MINUTE, 0);
        begin.set(Calendar.SECOND, 0);
        begin.set(Calendar.MILLISECOND, 0);
        Calendar end = (Calendar) date.clone();
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 59);
        for (int i = 0; i < size; i++) {
            if (allrecords.get(i).getDate().compareTo(begin) >= 0 &&
                    allrecords.get(i).getDate().compareTo(end) <= 0)
                records.add(allrecords.get(i));
        }
        RecordDetailAdapter adapter = new RecordDetailAdapter(getContext(), records, mode);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rvRecordDetail.setLayoutManager(llm);
        if (records.isEmpty()) {
            rvRecordDetail.setVisibility(View.GONE);
        } else {
            rvRecordDetail.setVisibility(View.VISIBLE);
        }
        rvRecordDetail.setAdapter(adapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivToolbarMostRight:
                final RecordDetailAdapter adapter = (RecordDetailAdapter) rvRecordDetail.getAdapter();
                if (mode == PocketAccounterGeneral.EDIT_MODE) {
                    boolean b = false;
                    if(selections != null)
                        for (int i = 0; i < selections.length; i++) {
                            if (selections[i]) {
                                b = true;
                                break;
                            }
                        }
                    if(b){
                        final WarningDialog warningDialog = new WarningDialog(getContext());
                        warningDialog.setText(getString(R.string.records_will_be_deleted));
                        warningDialog.setOnYesButtonListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                adapter.removeItems();
                                mode = PocketAccounterGeneral.NORMAL_MODE;
                                setMode(mode);
                                warningDialog.dismiss();
                            }
                        });
                        warningDialog.setOnNoButtonClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mode = PocketAccounterGeneral.NORMAL_MODE;
                                setMode(mode);
                                warningDialog.dismiss();
                            }
                        });
                        warningDialog.show();
                    }
                } else {
                    mode = PocketAccounterGeneral.EDIT_MODE;
                    setMode(mode);
                }

                break;
        }
    }

    private void setMode(final int mode) {
        final RecordDetailAdapter adapter = (RecordDetailAdapter) rvRecordDetail.getAdapter();
        adapter.setMode(mode);
        if (mode == PocketAccounterGeneral.NORMAL_MODE) {
            toolbarManager.setImageToSecondImage(R.drawable.pencil);

        } else {
            toolbarManager.setImageToSecondImage(R.drawable.ic_delete_black);
        }
        adapter.listenChanges();
    }

    public class RecordDetailAdapter extends RecyclerView.Adapter<RecordDetailAdapter.DetailViewHolder>{
        List<FinanceRecord> result;
        Context context;
        int mode = PocketAccounterGeneral.NORMAL_MODE;
        public RecordDetailAdapter(Context context, List<FinanceRecord> result, int mode){
            this.context = context;
            this.result = result;
            this.mode = mode;
            selections = new boolean[result.size()];
        }

        @Override
        public void onBindViewHolder(final DetailViewHolder holder, final int position) {
            int resId = context.getResources().getIdentifier(result.get(position).getCategory().getIcon(), "drawable", context.getPackageName());
            holder.ivRecordDetail.setImageResource(resId);
            holder.tvRecordDetailCategoryName.setText(result.get(position).getCategory().getName());
            holder.tvAccountName.setText(result.get(position).getAccount().getName());
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            String sign = "";

            if (result.get(position).getComment()==null||result.get(position).getComment().matches("")){
                holder.rlVisibleWhenHaveComment.setVisibility(View.GONE);
            }
            else {
                holder.tvRecordComment.setText(result.get(position).getComment());
                holder.rlVisibleWhenHaveComment.setVisibility(View.VISIBLE);
            }
            boolean keyik=true;
            for (PhotoDetails temp:result.get(position).getAllTickets()){
                File tmpFile=new File(temp.getPhotopath());
                File tmpCacheFile=new File(temp.getPhotopathCache());
                if(tmpFile.exists()&&tmpCacheFile.exists()){
                    keyik=false;
                    break;
                }
            }
            if(keyik){
                holder.rlVisibleWhenHaveTickets.setVisibility(View.GONE);
            }
            else{
                holder.rlVisibleWhenHaveTickets.setVisibility(View.VISIBLE);

            }

            LinearLayoutManager layoutManager
                    = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            holder.rvTickets.setLayoutManager(layoutManager);
            PhotoAdapter myTickedAdapter =new PhotoAdapter(result.get(position).getAllTickets(),
                    getContext(), new RecordEditFragment.OpenIntentFromAdapter() {
                @Override
                public void startActivityFromFragmentForResult(Intent intent) {
                    PocketAccounter.openActivity=true;
                    startActivity(intent);
                }
            },true);
            holder.rvTickets.hasFixedSize();
            holder.rvTickets.setAdapter(myTickedAdapter);

            if (result.get(position).getCategory().getType() == PocketAccounterGeneral.EXPENSE) {
                holder.tvRecordDetailCategoryAmount.setTextColor(ContextCompat.getColor(context, R.color.record_red));
                sign = "-";
            }
            else {
                holder.tvRecordDetailCategoryAmount.setTextColor(ContextCompat.getColor(context, R.color.record_green));
                sign = "+";
            }
            holder.tvRecordDetailCategoryAmount.setText(sign + decimalFormat.format(result.get(position).getAmount())+result.get(position).getCurrency().getAbbr());
            boolean subCatIsNull = (result.get(position).getSubCategory() == null);
            if (!subCatIsNull) {
               // holder.llSubCategories.setVisibility(View.GONE);
//                resId = context.getResources().getIdentifier(result.get(position).getSubCategory().getIcon(), "drawable", context.getPackageName());
                holder.tvRecordDetailCategoryName.setText(result.get(position).getCategory().getName() + ", " + result.get(position).getSubCategory().getName());

            }

            if (mode == PocketAccounterGeneral.NORMAL_MODE) {
                holder.chbRecordDetail.setVisibility(View.GONE);
                holder.ivRecordDetail.setVisibility(View.VISIBLE);
            }
            else {
                holder.chbRecordDetail.setVisibility(View.VISIBLE);
                holder.ivRecordDetail.setVisibility(View.GONE);
                holder.chbRecordDetail.setChecked(selections[position]);
            }
            final FinanceRecord financeRecord= result.get(position);
            holder.root.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mode == PocketAccounterGeneral.NORMAL_MODE) {
                        paFragmentManager.getFragmentManager().popBackStack();
                        paFragmentManager.displayFragment(new RecordEditFragment(null,
                                financeRecord.getDate(), financeRecord, PocketAccounterGeneral.DETAIL));
                    }
                    else {
                        holder.chbRecordDetail.setChecked(!holder.chbRecordDetail.isChecked());
                    }
                }
            });
            holder.chbRecordDetail.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    selections[position] = isChecked;
                }
            });
        }

        @Override
        public int getItemCount() {
            return result.size();
        }
        @Override
        public DetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_record_details, parent, false);
            DetailViewHolder viewHolder = new DetailViewHolder(v);
            return viewHolder;
        }

        public class DetailViewHolder extends RecyclerView.ViewHolder {
            public ImageView ivRecordDetail;
            public TextView tvRecordDetailCategoryName;
            public TextView tvRecordDetailCategoryAmount;
//            public TextView tvRecordDetailSubCategory;
            public TextView tvAccountName;
            public TextView tvRecordComment;
            public CheckBox chbRecordDetail;
            public LinearLayout rlVisibleWhenHaveComment;
            public LinearLayout rlVisibleWhenHaveTickets;
            public RecyclerView rvTickets;
            public View root;
            public DetailViewHolder(View view) {
                super(view);
                ivRecordDetail = (ImageView) view.findViewById(R.id.ivRecordDetail);
                tvRecordDetailCategoryName = (TextView) view.findViewById(R.id.tvRecordDetailCategoryName);
                tvRecordComment = (TextView) view.findViewById(R.id.tvComment);
                rvTickets = (RecyclerView) view.findViewById(R.id.rvTickets);
                tvRecordDetailCategoryAmount = (TextView) view.findViewById(R.id.tvRecordDetailCategoryAmount);
//                tvRecordDetailSubCategory = (TextView) view.findViewById(R.id.tvRecordDetailSubCategory);
                chbRecordDetail = (CheckBox) view.findViewById(R.id.chbRecordFragmentDetail);
                rlVisibleWhenHaveComment = (LinearLayout) view.findViewById(R.id.visibleIfCommentHave);
                rlVisibleWhenHaveTickets = (LinearLayout) view.findViewById(R.id.visibleIfTicketHave);
                tvAccountName = (TextView) view.findViewById(R.id.accountName);
                root = view;
            }
        }
        public void setMode(int mode) {
            this.mode = mode;
        }
        public void removeItems() {
            for (int i = selections.length-1; i >= 0; i--) {
                if (selections[i]) {
                    final List<PhotoDetails> tempok=result.get(i).getAllTickets();
                    (new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if(tempok!=null)
                            for (PhotoDetails temp:tempok) {
                                File forDeleteTicket=new File(temp.getPhotopath());
                                File forDeleteTicketCache=new File(temp.getPhotopathCache());
                                try {
                                    forDeleteTicket.delete();
                                    forDeleteTicketCache.delete();
                                }
                                catch (Exception o){
                                    o.printStackTrace();
                                }
                            }
                        }
                    })).start();
                    logicManager.deleteRecord(result.get(i));
                    dataCache.updateOneDay(date);
                    result.remove(i);
                }
             }
            notifyDataSetChanged();
            selections = new boolean[result.size()];
            for (int i = 0; i < selections.length; i++) {
                selections[i] = false;
            }
        }
        public void listenChanges() {
            for (int i = 0; i < selections.length; i++) {
                notifyItemChanged(i);
            }
        }
    }
}