package mx.cuernavaca.registro.request;


import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public abstract class RetrofitRequest {

    private final static int READ_TIME_OUT = 60 * 60;
    private final static int CONNECT_TIME_OUT = 60 * 60;

    /**
     * Modelo básico de todas las peticiones
     */
    public static <T> T create(Class<T> clazz) {
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor( new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request  = chain.request();
                Request.Builder builder = request.newBuilder();
                builder.addHeader("Content-type", "application/json");
                return chain.proceed(builder.build());
            }
        }).readTimeout(READ_TIME_OUT, TimeUnit.SECONDS).connectTimeout(CONNECT_TIME_OUT, TimeUnit.SECONDS).build();


        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(APIUrl.BASE)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        return retrofit.create(clazz);
    }

    /**
     * Crea el objeto RequestBody con los datos
     */
    public static RequestBody createBody(String datos) {
        return RequestBody.create( MediaType.parse("application/json"), datos);
    }
}
