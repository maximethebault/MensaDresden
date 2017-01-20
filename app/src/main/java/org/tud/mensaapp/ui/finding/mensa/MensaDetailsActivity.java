package org.tud.mensaapp.ui.finding.mensa;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.tud.mensaapp.R;
import org.tud.mensaapp.model.entity.Day;
import org.tud.mensaapp.model.entity.Meal;
import org.tud.mensaapp.model.entity.Mensa;
import org.tud.mensaapp.model.service.mensa.ErrorType;
import org.tud.mensaapp.model.service.mensa.FetchMensaListener;
import org.tud.mensaapp.model.service.mensa.MensaService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MensaDetailsActivity extends AppCompatActivity {

    public class MenuListAdapter extends RecyclerView.Adapter<MenuListAdapter.ViewHolder> {

        private List<Day> itemList;
        private int selectedPos = 0;
        private SimpleDateFormat daySdf;
        private SimpleDateFormat dateSdf;
        private SimpleDateFormat decodingSdf;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView dayTextView;
            public TextView dateTextView;

            public ViewHolder(View view) {
                super(view);
                dayTextView = (TextView) view.findViewById(R.id.item_day);
                dateTextView = (TextView) view.findViewById(R.id.item_date);
            }
        }

        public MenuListAdapter() {
            this(new ArrayList<Day>());
        }

        public MenuListAdapter(List<Day> itemList) {
            this.itemList = itemList;
            daySdf = new SimpleDateFormat("EEEE", Locale.getDefault());
            dateSdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
            decodingSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        }

        public void setItemList(List<Day> itemList) {
            this.itemList = itemList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.mensa_details_menu_item, parent, false);

            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.itemView.setSelected(selectedPos == position);
            try {
                Date date = decodingSdf.parse(itemList.get(position).getDate());
                holder.dayTextView.setText(daySdf.format(date));
                holder.dateTextView.setText(dateSdf.format(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    notifyItemChanged(selectedPos);
                    selectedPos = holder.getAdapterPosition();
                    notifyItemChanged(selectedPos);

                    menuListRecyclerView.smoothScrollToPosition(holder.getAdapterPosition());

                    Day day = itemList.get(holder.getAdapterPosition());
                    onDaySelected(day);
                }
            });
        }

        @Override
        public int getItemCount() {
            return itemList.size();
        }
    }

    private String mensaId;
    private ProgressBar menuListPorgressBar;
    private ProgressBar menuPorgressBar;
    private TextView menuText;

    private RecyclerView menuListRecyclerView;
    private MenuListAdapter menuListAdapter;

    private LinearLayoutManager menuListLayoutManagaer;

    private MensaService mensaService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mensa_details);

        mensaId = getIntent().getStringExtra("id");
        mensaService = MensaService.getInstance();

        Mensa mensa = mensaService.getMensaById(this, mensaId);

        TextView mensaName = (TextView) findViewById(R.id.details_mensa_name);
        mensaName.setText(mensa.getName());
        TextView mensaAddress = (TextView) findViewById(R.id.details_mensa_address);
        mensaAddress.setText(mensa.getAddress());


        final Toolbar toolbar = (Toolbar) findViewById(R.id.details_toolbar);
        toolbar.setTitleTextColor(Color.WHITE);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.details_title);

            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }

        menuListPorgressBar = (ProgressBar) findViewById(R.id.details_menu_list_loader);
        menuListRecyclerView = (RecyclerView) findViewById(R.id.details_menu_list);
        menuPorgressBar = (ProgressBar) findViewById(R.id.details_menu_loader);
        menuText = (TextView) findViewById(R.id.details_menu_text);


        menuListAdapter = new MenuListAdapter();

        menuListLayoutManagaer = new CenterLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        menuListRecyclerView.setLayoutManager(menuListLayoutManagaer);

        SnapHelper helper = new LinearSnapHelper();
        helper.attachToRecyclerView(menuListRecyclerView);

        menuListRecyclerView.setAdapter(menuListAdapter);
        menuListRecyclerView.setOnFlingListener(helper);

        mensaService.fetchDaysForMensaId(this, mensaId, new FetchMensaListener<List<Day>>() {
            @Override
            public void onStarted() {

            }

            @Override
            public void onSuccess(boolean wasMensaListUpdated, List<Day> results) {
                menuListPorgressBar.setVisibility(View.GONE);
                menuListRecyclerView.setVisibility(View.VISIBLE);
                List<Day> cleanResults = new ArrayList<>();
                if (results != null) {
                    for (Day day : results) {
                        if (!day.isClosed()) {
                            cleanResults.add(day);
                        }
                    }
                }
                menuListAdapter.setItemList(cleanResults);
                menuListAdapter.notifyDataSetChanged();
                if (cleanResults.size() > 0) {
                    onDaySelected(cleanResults.get(0));
                }
            }

            @Override
            public void onFail(ErrorType errorType, String error) {
                menuListPorgressBar.setVisibility(View.GONE);
                Toast.makeText(MensaDetailsActivity.this, "Failed to get list of menus", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void onDaySelected(Day day) {
        mensaService.fetchMeals(this, mensaId, day, new FetchMensaListener<List<Meal>>() {
            @Override
            public void onStarted() {
                menuPorgressBar.setVisibility(View.VISIBLE);
                menuText.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(boolean wasMensaListUpdated, List<Meal> results) {
                menuPorgressBar.setVisibility(View.GONE);
                String str = "";
                for (Meal meal : results) {
                    str += meal.getName() + "\n\n";
                }
                menuText.setText(str);
                menuText.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFail(ErrorType errorType, String error) {
                menuPorgressBar.setVisibility(View.GONE);
                Toast.makeText(MensaDetailsActivity.this, "Failed to get menu", Toast.LENGTH_LONG).show();
            }
        });
    }
}
