package com.nazmiev.radik.vkclient;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.rx2androidnetworking.Rx2AndroidNetworking;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class SearchableSpinner2 extends LinearLayout {
    private String _type = "";
    private String _button_type = "";
    private View main_view;
    private Activity _activity;
    private Context _context;
    private View customView;
    private Object selected_item;
    private ListView lv;
    private Object selected_item_s;
    private ArrayList<SearchableSpinnerItems> cdata = new ArrayList<SearchableSpinnerItems>();
    private String s = "";
    private static Handler handler = new Handler(Looper.getMainLooper());


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SearchableSpinner2(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

    }

    public SearchableSpinner2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    public SearchableSpinner2(Context context){
        super(context);

    }

    public SearchableSpinner2(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;


        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.SearchableSpinner2);
        CharSequence type_cs = arr.getString(R.styleable.SearchableSpinner2_type);
        CharSequence button_type_cs = arr.getString(R.styleable.SearchableSpinner2_button_type);
        if (type_cs != null) {
            _type = type_cs.toString();
        }
        if (button_type_cs != null) {
            _button_type = button_type_cs.toString();
        }
        arr.recycle();

        Activity ac = (Activity) context;
        _activity = ac;
        main_view = ac.getWindow().getDecorView().findViewById(android.R.id.content);

        init();
    }

    @Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public boolean onTouch(View v, MotionEvent event){
        return true;
    }


    private void init() {
        Activity activ = (Activity) _context;
        s = activ.getIntent().getStringExtra("current_acc");
        if(s == null) {
            Db db = new Db(_context);
            s = db.getDefAcc()[1];
        }
        if(_button_type.equals("1")) {
            final View rootView = inflate(getContext(), R.layout.searchable_spinner2_main, this);
//            Button btn = rootView.findViewById(R.id.button2);
//            btn.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    openPopup();
//                }
//            });

            selected_item = rootView.findViewById(R.id.selectedItemText);

            TextView selectedItemText = rootView.findViewById(R.id.selectedItemText);
            selectedItemText.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    openPopup();
                }
            });
        }
        else{
            View rootView = inflate(getContext(), R.layout.searchable_spinner2_main_button_style, this);
            TextView selectedItemText = rootView.findViewById(R.id.selectedItemText);
            TextView sspLabel = rootView.findViewById(R.id.sspLabel);
            if(_type.equals("2")) {
                sspLabel.setText(R.string.select_country);
            }
            else{
                sspLabel.setText(R.string.select_city);
            }
            LinearLayout selectedItem = rootView.findViewById(R.id.selectedItem);
            selectedItem.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    openPopup();
                }
            });

            selected_item = selectedItemText;
        }

        LayoutInflater inflater = (LayoutInflater) _activity.getSystemService(LAYOUT_INFLATER_SERVICE);
        customView = inflater.inflate(R.layout.searchable_spiner2_popup,null);

        getData("");
    }

    public String getType(){
        return _type;
    }

    public void setType(String type){
        _type = type;
    }

    private void openPopup(){
        final PopupWindow mPopupWindow = new PopupWindow(
                customView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        EditText search_text = customView.findViewById(R.id.searchText);
        search_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(_type.equals("1")) {
                    cdata.clear();
                    getMainCitys(charSequence.toString());
                }
                else{
                    getData(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        ListView city_list = customView.findViewById(R.id.city_list);
        city_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                setSelectedItem(position);
                mPopupWindow.dismiss();
            }
        });


        Button close_button = customView.findViewById(R.id.closeButton);
        close_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mPopupWindow.dismiss();
            }
        });

        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setTouchable(true);
        mPopupWindow.showAtLocation(main_view, Gravity.CENTER,0,0);
    }

    private void getData(String query){
        Db db = new Db(_context);

        lv = customView.findViewById(R.id.city_list);
        if(!query.equals("")){
            query = "&q=" + query;
        }

        if(_type.equals("1")) {
            if(db.getSettingsCount(s) == 1){
                getDefaultCity();
            }
            else{
                getMainCitys(query);
            }
        }
        if(_type.equals("2")){
            Rx2AndroidNetworking.get("https://api.vk.com/method/database.getCountries?need_all=0&access_token={s}&v=5.8")
                    .addPathParameter("s", s)
                    .build()
                    .getJSONObjectObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<JSONObject>() {
                        @Override
                        public void onError(Throwable e) {
                            // handle error
                            e.printStackTrace();
                        }

                        @Override
                        public void onComplete() {

                        }

                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(JSONObject response) {
                            try {
                                JSONObject resp = response.getJSONObject("response");
                                JSONArray items = resp.getJSONArray("items");
                                if(items.length() > 0) {
                                    for (int i = 0; i < items.length(); i++) {
                                        String name = "";
                                        if (items.getJSONObject(i).toString().contains("region")) {
                                            name = items.getJSONObject(i).getString("title") + "\n" + items.getJSONObject(i).getString("region");
                                        } else {
                                            name = items.getJSONObject(i).getString("title");
                                        }
                                        String id = items.getJSONObject(i).getString("id");
                                        SearchableSpinnerItems ctr = new SearchableSpinnerItems(name, id);
                                        cdata.add(ctr);
                                    }
                                    handler.post(new Runnable() {
                                        public void run() {
                                            SearchableListAdapter listAdapter = new SearchableListAdapter(_context, cdata);

                                            lv.setAdapter(listAdapter);
                                            lv.invalidateViews();
                                            listAdapter.notifyDataSetChanged();
                                            setSelectedItem(0);
                                        }
                                    });

                                    //cadapter.notifyDataSetChanged();
                                }
                                else{
                                    Toast toast = Toast.makeText(_context, "Город не найден", Toast.LENGTH_SHORT);
                                    toast.show();
                                }

                            } catch (JSONException e) {
                                Toast toast = Toast.makeText(_context, e.getMessage(), Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }
                    });
        }
    }

    private void setSelectedItem(int position){
        try {
            ListAdapter lv_adapter = lv.getAdapter();
            SearchableSpinnerItems city = (SearchableSpinnerItems) lv_adapter.getItem(position);
            ((TextView)selected_item).setText(city._name);

            selected_item_s = city;
            if (_type.equals("2")) {
                GlobalVariables.selected_country = city;
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public Object getSelectedItem(){
        return selected_item_s;
    }

    private void getDefaultCity(){
        Db db = new Db(_context);
        Integer def_city = db.getUserCity(s);

        String url = "https://api.vk.com/method/database.getCitiesById?access_token=" + s + "&city_ids=" + def_city.toString() + "&v=5.73";

        final GetRequest gr = new GetRequest();
        cdata.clear();
        gr.Set(url, _context, new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    JSONArray items = result.getJSONArray("response");
                    for(int i = 0; i < items.length(); i++){
                        String name = items.getJSONObject(i).getString("title");
                        String id = items.getJSONObject(i).getString("id");
                        SearchableSpinnerItems ctr = new SearchableSpinnerItems(name, id);
                        cdata.add(ctr);
                    }
                    getMainCitys("");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                InternetErrors.Show(error, _context);
            }
        });
    }

    private void getMainCitys (String query){
        if(!query.equals("")){
            query = "&q=" + query;
        }
        SearchableSpinnerItems selected_country = (SearchableSpinnerItems) GlobalVariables.selected_country;
        String country = "1";
        if(selected_country != null) {
            country = selected_country.getId();
        }

        Rx2AndroidNetworking.get("https://api.vk.com/method/database.getCities?need_all=0&access_token={s}{query}&country_id={sel_country}&count=20&v=5.8")
                .addPathParameter("s", s)
                .addPathParameter("sel_country", country)
                .addPathParameter("query", query)
                .build()
                .getJSONObjectObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<JSONObject>() {
                    @Override
                    public void onError(Throwable e) {
                        // handle error
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(JSONObject response) {
                        try {
                            JSONObject resp = response.getJSONObject("response");
                            JSONArray items = resp.getJSONArray("items");
                            if(items.length() > 0) {
                                for (int i = 0; i < items.length(); i++) {
                                    String name = "";
                                    if (items.getJSONObject(i).toString().contains("region")) {
                                        name = items.getJSONObject(i).getString("title") + "\n" + items.getJSONObject(i).getString("region");
                                    } else {
                                        name = items.getJSONObject(i).getString("title");
                                    }
                                    String id = items.getJSONObject(i).getString("id");
                                    SearchableSpinnerItems ctr = new SearchableSpinnerItems(name, id);
                                    cdata.add(ctr);
                                }

                                handler.post(new Runnable() {
                                    public void run() {
                                        SearchableListAdapter listAdapter = new SearchableListAdapter(_context, cdata);
                                        lv.setAdapter(listAdapter);
                                        lv.invalidateViews();
                                        setSelectedItem(0);
                                    }
                                });

                                //listAdapter.notifyDataSetChanged();
                                //cadapter.notifyDataSetChanged();
                            }
                            else{
                                Toast toast = Toast.makeText(_context, "Город не найден", Toast.LENGTH_SHORT);
                                toast.show();
                            }

                        } catch (JSONException e) {
                            Toast toast = Toast.makeText(_context, e.getMessage(), Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                });
    }
}
