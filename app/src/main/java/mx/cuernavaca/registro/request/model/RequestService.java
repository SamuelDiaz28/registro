package mx.cuernavaca.registro.request.model;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface RequestService {

    @POST
    Call<String> getPromoters (@Url String url);

    @POST
    Call<String> sendData(@Body RequestBody data, @Url String url);

}
