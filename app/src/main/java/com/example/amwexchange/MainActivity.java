package com.example.amwexchange;

import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.android.volley.VolleyError;

public class MainActivity extends AppCompatActivity {
    public  TextView wartoscWaluty;
    private TextView wartoscPoPrzewalutowaniu;
    private TextView watoscWKantorze;
    private TextView znakWaluty;
    private EditText pieniadzeDoPrzewalutowania;
    private Button oblicz;
    private Button odswiezKurs;
    private String walutaZagraniczna;
    private String walutaNasza;
    private RequestQueue kolejka;
    private TextView pierwszaWaluta;
    private Switch awitch;
    private DrawerLayout drawer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Ustalenie zmiennych oraz ustawienie początkowych walut
        rozpoczecie();

        //Ustalenie zmiennych potrzebnych do overlapu (to co wyjezdza z boku)
        NavigationView navigationView=findViewById(R.id.widokNawigacyjny);
        navigationView.bringToFront();
        Toolbar toolbar=findViewById(R.id.toolbar);
        drawer=findViewById(R.id.drawer_Layout);
        kolejka = Volley.newRequestQueue(this);

        //wywołanie funkcji która ustala przelicznik
        jsonWaluty();

        //cały overlap z akcją która ma się wykonać kiedy użyjesz któregoś z przycisków (każdy objekt jest przyciskiem)
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(this,drawer,toolbar,R.string.navigacja_otwarta,R.string.navigacja_zamknieta);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if(menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);
                drawer.closeDrawers();
                awitch.setChecked(false);
                switch(menuItem.getItemId()){
                    case R.id.aud:
                        walutaZagraniczna="AUD";
                        break;
                    case R.id.chf:
                        walutaZagraniczna="CHF";
                        break;
                    case R.id.eur:
                        walutaZagraniczna="EUR";
                        break;
                    case R.id.usd:
                        walutaZagraniczna="USD";
                        break;
                    case R.id.czk:
                        walutaZagraniczna="CZK";
                        break;
                    case R.id.gbp:
                        walutaZagraniczna="GBP";
                        break;
                    case R.id.huf:
                        walutaZagraniczna="HUF";
                        break;
                    case R.id.jpy:
                        walutaZagraniczna="JPY";
                        break;
                    case R.id.nok:
                        walutaZagraniczna="NOK";
                        break;
                    case R.id.uah:
                        walutaZagraniczna="UAH";
                        break;

                        //sprawdzenie czy switch jest wciśnięty, (brak buggów przy zamianie waluty)
                }if(awitch.isChecked()) przestawienieWaluty();

                //ustalenie nowego przelicznika i zerowanie pól ze starymi wartościami
                jsonWaluty();
                wartoscPoPrzewalutowaniu.setText(null);
                watoscWKantorze.setText(null);
                return true;
            }
        });
        awitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                przestawienieWaluty();
            }
        });

        //jeżeli użyjesz buttona "Oblicz" wykonaj obliczenie otrzymanych pieniędzy
        oblicz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ustalenieWaluty();
            }
        });

        //jeżeli użyjesz buttona "Odśwież kurs" wykonaj zapytanie i ustawienie przelicznika
        odswiezKurs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jsonWaluty();
            }
        });
    }

    public void rozpoczecie(){
        ustalenieZmiennych();
        walutaZagraniczna = "eur";
        walutaNasza="pln";
    }


    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START);
        else super.onBackPressed();
    }

    private void przestawienieWaluty(){
        String pierwszaWalut=pierwszaWaluta.getText().toString();
        String znakWalut=znakWaluty.getText().toString();
        String pomocnicza=null;
        pomocnicza=pierwszaWalut;
        pierwszaWalut=znakWalut;
        znakWalut=pomocnicza;
        pierwszaWaluta.setText(pierwszaWalut);
        znakWaluty.setText(znakWalut);
        ustalenieWaluty();

    }

    private void ustalenieZmiennych() {
        oblicz = findViewById(R.id.button2);
        odswiezKurs = findViewById(R.id.button3);
        wartoscWaluty = findViewById(R.id.textView9);
        wartoscPoPrzewalutowaniu = findViewById(R.id.textView10);
        watoscWKantorze = findViewById(R.id.textView14);
        pieniadzeDoPrzewalutowania = findViewById(R.id.editText);
        znakWaluty = findViewById(R.id.textView4);
        pierwszaWaluta=findViewById(R.id.textView5);
        awitch=findViewById(R.id.switch4);
    }

    private void ustalenieWaluty() {
        try {
            double kursik = Double.parseDouble(wartoscWaluty.getText().toString());
            double przelicznik = Double.parseDouble(pieniadzeDoPrzewalutowania.getText().toString());
            if(!awitch.isChecked()) {
                wartoscPoPrzewalutowaniu.setText(String.format("%.2f", przelicznik/kursik  ) + " " + walutaZagraniczna.toUpperCase());
                watoscWKantorze.setText(String.format("%.2f", (( przelicznik/kursik) * 0.981)) + " " + walutaZagraniczna.toUpperCase());
            }
            else{
                wartoscPoPrzewalutowaniu.setText(String.format("%.2f", kursik * przelicznik) + " " + walutaNasza.toUpperCase());
                watoscWKantorze.setText(String.format("%.2f", ((kursik * przelicznik) * 0.981)) + " " + walutaNasza.toUpperCase());}

        }catch(Exception e){
            pieniadzeDoPrzewalutowania.setHint("Wpisz jakąś liczbę!");
        }
    }

    private void jsonWaluty() {
        if(!awitch.isChecked()) znakWaluty.setText(walutaZagraniczna.toUpperCase());
        String url = "http://api.nbp.pl/api/exchangerates/rates/a/"+walutaZagraniczna+"/?format=json";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray=response.getJSONArray("rates");
                            JSONObject A=jsonArray.getJSONObject(0);
                            String kurs=A.getString("mid");
                            wartoscWaluty.setText(kurs);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });kolejka.add(request);
    }

}