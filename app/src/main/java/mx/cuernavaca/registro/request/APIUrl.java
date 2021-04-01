package mx.cuernavaca.registro.request;

public abstract class APIUrl {


   public static final String BASE = "http://google/";

   public static final String GET_PROMOTERS = "http://172.241.131.200:8080/ElectoralVote/api/usuario/app/get/promotores";
   public static final String SEND_DATA = "http://172.241.131.200:8080/ElectoralVote/api/usuario/app/insert/registro";
}
