package mx.cuernavaca.registro;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import mx.cuernavaca.registro.request.APIUrl;
import mx.cuernavaca.registro.request.RetrofitRequest;
import mx.cuernavaca.registro.request.model.RequestService;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.regula.documentreader.api.DocumentReader;
import com.regula.documentreader.api.completions.IDocumentReaderCompletion;
import com.regula.documentreader.api.completions.IDocumentReaderInitCompletion;
import com.regula.documentreader.api.enums.DocReaderAction;
import com.regula.documentreader.api.enums.Scenario;
import com.regula.documentreader.api.enums.eGraphicFieldType;
import com.regula.documentreader.api.enums.eVisualFieldType;
import com.regula.documentreader.api.errors.DocumentReaderException;
import com.regula.documentreader.api.results.DocumentReaderResults;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ArrayList<String> promotersArray = new ArrayList<String>();
    private AutoCompleteTextView autoCompleteText;
    private EditText edtNombre;
    private EditText edtPaterno;
    private EditText edtMaterno;
    private EditText edtEdad;
    private EditText edtSexo;
    private EditText edtSeccion;
    private EditText edtCalle;
    private EditText edtColonia;
    private EditText edtCp;
    private EditText edtTelefono;
    private EditText edtCorreo;
    private EditText edtFacebook;
    private EditText edtTwitter;
    private EditText edtCompania;
    private EditText edtApoyo;
    private EditText edtClaveElectoral;

    private ImageButton imgID;
    private Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        promotersArray = (ArrayList<String>) getIntent().getStringArrayListExtra("promotersArray");

        startComponents();

    }

    private void startComponents() {

        imgID = findViewById(R.id.imgId);
        btnSend = findViewById(R.id.btnSend);

        autoCompleteText = findViewById(R.id.txtAutoComplete);
        edtNombre = findViewById(R.id.edtName);
        edtPaterno = findViewById(R.id.edtPaterno);
        edtMaterno = findViewById(R.id.edtMaterno);
        edtEdad = findViewById(R.id.edtEdad);
        edtSexo = findViewById(R.id.edtSexo);
        edtSeccion = findViewById(R.id.edtSeccion);
        edtCalle = findViewById(R.id.edtCalle);
        edtColonia = findViewById(R.id.edtColonia);
        edtCp = findViewById(R.id.edtCp);
        edtTelefono = findViewById(R.id.edtTelefono);
        edtCorreo = findViewById(R.id.edtMail);
        edtFacebook = findViewById(R.id.edtFacebook);
        edtTwitter = findViewById(R.id.edtTwitter);
        edtCompania = findViewById(R.id.edtCompania);
        edtApoyo = findViewById(R.id.edtApoyo);
        edtClaveElectoral = findViewById(R.id.edtElectorKey);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, promotersArray);
        autoCompleteText.setAdapter(arrayAdapter);

        btnSend.setOnClickListener(this);


    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            Log.i(">>REGULA INIT<<", "ENTRO 1");
            InputStream licInput = getResources().openRawResource(R.raw.regula);
            int available = licInput.available();
            byte[] license = new byte[available];
            //noinspection ResultOfMethodCallIgnored
            licInput.read(license);

            DocumentReader.Instance().initializeReader(MainActivity.this, license, new IDocumentReaderInitCompletion() {
                @Override
                public void onInitCompleted(boolean success, DocumentReaderException e) {

                    DocumentReader.Instance().customization().edit().setShowHelpAnimation(false).apply();

                    //Initialization successful
                    if (success) {
                        imgID.setOnClickListener(MainActivity.this);

                        Log.i(">>REGULA INIT<<", "ENTRO 2");
                        // set the scenario value
                        DocumentReader.Instance().processParams().scenario = Scenario.SCENARIO_MRZ_OR_OCR;
                    } else {
                        Toast.makeText(MainActivity.this, "Init failed:" + e, Toast.LENGTH_LONG).show();
                        Log.i(">>REGULA INIT<<", "ENTRO 3");
                    }
                }
            });

            licInput.close();

        } catch (Exception e) {
            Log.i(">>REGULA INIT<<", "ENTRO 4");
            e.printStackTrace();
        }

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgId:
                DocumentReader.Instance().showScanner(MainActivity.this, completionScanId);
                break;

            case R.id.btnSend:
                if (edtNombre.getText().toString().isEmpty() ||
                        edtPaterno.getText().toString().isEmpty() ||
                        edtMaterno.getText().toString().isEmpty() ||
                        edtEdad.getText().toString().isEmpty() ||
                        edtSexo.getText().toString().isEmpty() ||
                        edtSeccion.getText().toString().isEmpty() ||
                        edtCalle.getText().toString().isEmpty() ||
                        edtColonia.getText().toString().isEmpty() ||
                        edtCp.getText().toString().isEmpty() ||
                        edtTelefono.getText().toString().isEmpty() ||
                        edtClaveElectoral.getText().toString().isEmpty() ||
                        autoCompleteText.getText().toString().isEmpty()) {

                    Toast.makeText(MainActivity.this, "Falta llenar campos obligatorios", Toast.LENGTH_LONG).show();
                } else {
                    sendData();
                }
                break;
        }

    }

    private IDocumentReaderCompletion completionScanId = new IDocumentReaderCompletion() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onCompleted(int action, DocumentReaderResults results, DocumentReaderException e) {
            if (action == DocReaderAction.COMPLETE) {
                displayResults(results);
            } else {
                if (action == DocReaderAction.CANCEL) {
                    Toast.makeText(MainActivity.this, "Escaneo cancelado", Toast.LENGTH_LONG).show();
                } else if (action == DocReaderAction.ERROR) {
                    Toast.makeText(MainActivity.this, "Error: " + e, Toast.LENGTH_LONG);
                }
            }
        }
    };

    private void displayResults(DocumentReaderResults results) {
        try {

            String strMain = results.getTextFieldValueByType(eVisualFieldType.FT_SURNAME_AND_GIVEN_NAMES);
            String[] arrSplit = strMain.split("\\s+", 3);
           for (int i = 0; i <= arrSplit.length; i++) {
                switch (i){
                    case 0 :
                        edtPaterno.setText(arrSplit[i]);

                    case 1:
                        edtMaterno.setText(arrSplit[i]);

                    case 2:
                        edtNombre.setText(arrSplit[i]);
                }
            }


            edtEdad.setText(results.getTextFieldValueByType(eVisualFieldType.FT_AGE));
            edtSexo.setText(results.getTextFieldValueByType(eVisualFieldType.FT_SEX));
            edtSeccion.setText(results.getTextFieldValueByType(eVisualFieldType.FT_SECTION));
            edtCalle.setText(results.getTextFieldValueByType(eVisualFieldType.FT_ADDRESS));
            edtColonia.setText(results.getTextFieldValueByType(eVisualFieldType.FT_MAILING_ADDRESS_CITY));
            edtCp.setText(results.getTextFieldValueByType(eVisualFieldType.FT_MAILING_ADDRESS_POSTAL_CODE));
            edtClaveElectoral.setText(results.getTextFieldValueByType(eVisualFieldType.FT_VOTER_KEY));

            Bitmap documentId = results.getGraphicFieldImageByType(eGraphicFieldType.GF_DOCUMENT_IMAGE);
            if (documentId != null) {
                double aspectRatio = (double) documentId.getWidth() / (double) documentId.getHeight();
                documentId = Bitmap.createScaledBitmap(documentId, (int) (480 * aspectRatio), 480, false);
                imgID.setImageBitmap(documentId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        checkResults();
    }

    private void checkResults() {
        if (edtNombre.getText().toString().isEmpty() ||
                edtPaterno.getText().toString().isEmpty() ||
                edtMaterno.getText().toString().isEmpty() ||
                edtEdad.getText().toString().isEmpty() ||
                edtSexo.getText().toString().isEmpty() ||
                edtSeccion.getText().toString().isEmpty() ||
                edtCalle.getText().toString().isEmpty() ||
                edtColonia.getText().toString().isEmpty() ||
                edtCp.getText().toString().isEmpty() ||
                edtClaveElectoral.getText().toString().isEmpty()) {

            Toast.makeText(MainActivity.this, "Algo salio mal en el escaneo, volver a intentar o llenar manualmente", Toast.LENGTH_LONG).show();
        }
    }

    private void clearForm() {

        edtNombre.getText().clear();
        edtPaterno.getText().clear();
        edtMaterno.getText().clear();
        edtEdad.getText().clear();
        edtSexo.getText().clear();
        edtSeccion.getText().clear();
        edtCalle.getText().clear();
        edtColonia.getText().clear();
        edtCp.getText().clear();
        edtTelefono.getText().clear();
        edtCompania.getText().clear();
        edtApoyo.getText().clear();
        edtClaveElectoral.getText().clear();
        autoCompleteText.getText().clear();
    }

    private void sendData() {
        String data = null;
        try {

            JSONObject registro = new JSONObject();

            registro.put("nombre", edtNombre.getText().toString().trim());
            registro.put("paterno", edtPaterno.getText().toString().trim());
            registro.put("materno", edtMaterno.getText().toString().trim());
            registro.put("edad", edtEdad.getText().toString().trim());
            registro.put("sexo", edtSexo.getText().toString().trim());
            registro.put("seccion", edtSeccion.getText().toString().trim());
            registro.put("calle", edtCalle.getText().toString().trim());
            registro.put("colonia", edtColonia.getText().toString().trim());
            registro.put("cp", edtCp.getText().toString().trim());
            registro.put("telefono", edtTelefono.getText().toString().trim());
            registro.put("mail", edtCorreo.getText().toString().trim());
            registro.put("clave_ele", edtClaveElectoral.getText().toString().trim());
            registro.put("notas", "Ninguna");
            registro.put("facebook", edtFacebook.getText().toString().trim());
            registro.put("twiter", edtTwitter.getText().toString().trim());
            registro.put("compania", edtCompania.getText().toString().trim());
            registro.put("apoyo", edtApoyo.getText().toString().trim());
            registro.put("registro", autoCompleteText.getText().toString().trim());

            data = registro.toString();

            RequestService service = RetrofitRequest.create(RequestService.class);
            RequestBody body = RetrofitRequest.createBody(data);
            Call<String> response = service.sendData(body, APIUrl.SEND_DATA);

            response.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.code() != 200) {
                        Toast.makeText(MainActivity.this, "Ocurrió un error de envio \n" + response.message(), Toast.LENGTH_LONG).show();
                        Log.i(">>>RESPONSE<<<", " Entro code: " + String.valueOf(response.code()));
                        return;
                    }

                    try {
                        JSONObject jres = new JSONObject(response.body());
                        Boolean res = jres.getBoolean("Ok");
                        if (res){
                            Toast.makeText(MainActivity.this, "Registro Exitoso", Toast.LENGTH_LONG).show();
                            clearForm();
                        }else {
                            String message = jres.getString("message");
                            Toast.makeText(MainActivity.this, "Error: " + message, Toast.LENGTH_LONG);
                        }
                        Log.i(">>>Data<<<", res.toString());


                    } catch (JSONException e) {
                        Log.e("MainActivity", "Error al convertir en json los datos\n" + e.getMessage());
                    }

                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } catch (JSONException e){
            e.printStackTrace();
        }
    }
}