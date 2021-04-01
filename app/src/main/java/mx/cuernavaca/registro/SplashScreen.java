package mx.cuernavaca.registro;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.regula.documentreader.api.DocumentReader;
import com.regula.documentreader.api.completions.IDocumentReaderInitCompletion;
import com.regula.documentreader.api.completions.IDocumentReaderPrepareCompletion;
import com.regula.documentreader.api.enums.Scenario;
import com.regula.documentreader.api.errors.DocumentReaderException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;

import mx.cuernavaca.registro.request.APIUrl;
import mx.cuernavaca.registro.request.RetrofitRequest;
import mx.cuernavaca.registro.request.model.RequestService;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashScreen extends AppCompatActivity {

    private Handler handler = new Handler();
    private ArrayList<String> listdata = new ArrayList<String>();
    private TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_splash_screen);

        getPromoters();
        textView = findViewById(R.id.txtLoading);
    }

   /* private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!isFinishing()){
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        }
    };*/


    @Override
    protected void onResume() {
        super.onResume();
        if (!DocumentReader.Instance().getDocumentReaderIsReady()) {
            textView.setText("Inicializando...");
            //Reading the license from raw resorce file
            try {
                InputStream licInput = getResources().openRawResource(R.raw.regula);
                int available = licInput.available();
                final byte[] license = new byte[available];
                licInput.read(license);

                //Preparing database files, it will be downloaded from network only one time and stored on user device
                DocumentReader.Instance().prepareDatabase(SplashScreen.this, "Full", new IDocumentReaderPrepareCompletion() {
                    @Override
                    public void onPrepareProgressChanged(int i) {
                        textView.setText("Descargando DB " + i + "%");
                    }

                    @Override
                    public void onPrepareCompleted(boolean b, DocumentReaderException e) {
                        getPromoters();
                    }


                });

                licInput.close();

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //handler.removeCallbacks(runnable);
    }

    private void getPromoters(){
        RequestService service = RetrofitRequest.create(RequestService.class);
        Call<String> response = service.getPromoters(APIUrl.GET_PROMOTERS);
        response.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                if (response.code() != 200) {
                    Toast.makeText(SplashScreen.this, "Ocurrió un error de obtencion \n" + response.message(), Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    JSONObject jres = new JSONObject(response.body());

                    JSONObject jsData = jres.getJSONObject("data");
                    boolean val = jres.getBoolean("OK");

                    if(val){
                        JSONArray arrayPromoters = jsData.getJSONArray("Promotores");
                        if (arrayPromoters != null) {
                            for (int i = 0; i < arrayPromoters.length(); i++) {
                                JSONObject object =(JSONObject) arrayPromoters.get(i);
                                listdata.add(object.getString("nombre"));
                            }
                        }

                        Intent i = new Intent(SplashScreen.this, MainActivity.class);
                        i.putExtra("promotersArray", listdata);
                        startActivity(i);
                        finish();

                    }


                }catch (JSONException e){
                    Log.e("SplashSreen", "Error al convertir en json los datos\n" + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "No ha sido posible inicializar la aplicación, reiniciela e intentelo mas tarde: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });

    }
}