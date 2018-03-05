package com.pmdm.mijuego;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class MiJuego extends ApplicationAdapter implements InputProcessor, ApplicationListener {

    // Atributo en el que se cargará la hoja de sprites del mosquetero.
	Texture img;

    //Atributo que permite dibujar imágenes 2D, en este caso el sprite.
    SpriteBatch batch;

    //Objeto que recoge el mapa de baldosas.
	private TiledMap mapa;

    //Objeto con el que se pinta el mapa de baldosas.
	private OrthogonalTiledMapRenderer mapaRenderer;

    //Cámara que nos da la vista del juego.
	private OrthographicCamera camara;

    //​Constantes que indican el número de filas y columnas de la hoja de sprites.
	private static final int FRAME_COLS=3, FRAME_ROWS=4;

	//Animación del jugador.
	private Animation jugador;

    //​Animaciones para cada una de las direcciones de movimiento del personaje del jugador.
    private Animation jugadorArriba​​;
    private Animation jugadorDerecha​​;
    private Animation jugadorAbajo​​;
    private Animation jugadorIzquierda​​;

    //​Posición en el eje de coordenadas actual del jugador.
    private float jugadorX​, jugadorY​​;

    /*Este atributo indica el tiempo en segundos transcurridos
    desde que se inicia la animación, servirá para determinar cual
    es el frame que se debe representar.*/
    private float stateTime​​;

    //Contendrá el frame que se va a mostrar en cada momento.
    private TextureRegion cuadroActual​​;

    //Tamaño del mapa de baldosas.
    private int anchoMapa, altoMapa;

    //Matriz de obstáculos.
    boolean [][] obstaculo;

    //Atributos que indican la anchura y la altura de un tile del mapa de baldosas.
    int anchoCelda, altoCelda;

    //Atributos que indican la anchura y altura del sprite animado del jugador.
    int anchoJugador, altoJugador;

    //Animaciones posicionales relacionadas con los NPC del juego.
    private Animation noJugadorArriba;
    private Animation noJugadorDerecha;
    private Animation noJugadorAbajo;
    private Animation noJugadorIzquierda;

    //Array con los objetos Animation de los NPC.
    private Animation[] noJugador;

    //Atributos que indican la anchura y altura del sprite animado de los NPC.
    int anchoNoJugador, altoNoJugador;

    //Posición inicial X de cada uno de los NPC.
    private float[] noJugadorX;

    //Posición inicial Y de cada uno de los NPC.
    private float[] noJugadorY;

    //Posición final X de cada uno de los NPC.
    private float[] destinoX;

    //Posición final Y de cada uno de los NPC.
    private float[] destinoY;

    //Número de NPC que van a aparecer en el juego.
    private static final int numeroNPCs = 20;

    /*
    Este atributo indica el tiempo en segundos transcurridos desde que se inicia la animación
    de los NPC , servirá para determinar cual es el frame que se debe representar.
    */
    private float stateTimeNPC;

    //Música del juego.
    Music musica;

    //Sonidos del juego.
    Sound sonidoColisionEnemigo, sonidoPasos, sonidoObstaculo;

    //Posición de la pulsación en pantalla.
    Vector3 posicion;
	
	@Override
	public void create () {

        /*Creamos una cámara y la vinculamos con el lienzo del juego.
        En este caso le damos unos valores de tamaño que haga que el juego
        se muestre de forma idéntica en todas las plataformas.*/
		camara=new OrthographicCamera(800, 480);

        //Posicionamos la vista de la cámara para que su vértice inferior izquierdo sea (0,0).
		camara.position.set(camara.viewportWidth / 2f, camara.viewportHeight / 2f, 0);

        //​Indicamos que los eventos de entrada sean procesados por esta clase.
		Gdx.input.setInputProcessor(this);

		//Actualizamos la cámara.
		camara.update();

        //Cargamos la imagen del mosquetero en el objeto img de la clase Texture.
        img = new Texture(Gdx.files.internal("mosquetero.png"));

        //Sacamos los frames de img en un array de TextureRegion.
        TextureRegion[][] tmp = TextureRegion.split(img, img.getWidth() / FRAME_COLS,
                img.getHeight() / FRAME_ROWS);

        /*
        Creamos las distintas animaciones, teniendo en cuenta que el tiempo de
        muestra de cada frame será de 150 milisegundos, y que les pasamos las
        distintas filas de la matriz tmp a las mismas
        */
        jugadorArriba​​ = new Animation(0.150f, tmp[0]);
        jugadorDerecha​​ = new Animation(0.150f, tmp[1]);
        jugadorAbajo​​ = new Animation(0.150f, tmp[2]);
        jugadorIzquierda​​ = new Animation(0.150f, tmp[3]);

        //​En principio se utiliza la animación del jugador arriba como animación por defecto.
        jugador = jugadorAbajo​​;

        //Posición inicial del jugador.
        jugadorX​ = jugadorY​​ = 500;

        //Ponemos a cero el atributo stateTime, que marca el tiempo de ejecución de la animación.
        stateTime​​ = 0f;

        //​ Creamos el objeto SpriteBatch que nos permitirá representar adecuadamente el sprite ​en el método render.
        batch= new SpriteBatch();

        //Cargamos el mapa de baldosas desde la carpeta de assets.
		mapa =new TmxMapLoader().load("miTile.tmx");
		mapaRenderer=new OrthogonalTiledMapRenderer(mapa);

        /*
        Determinamos el alto y ancho del mapa de baldosas.
        Para ello necesitamos extraer la capa base del mapa y,
        a partir de ella, determinamos el número de celdas a lo ancho y alto,
        así como el tamaño de la celda, que multiplicando por el número de celdas
        a lo alto y ancho, da como resultado el alto y ancho en pixeles del mapa.
        */
        TiledMapTileLayer capa = (TiledMapTileLayer) mapa.getLayers().get(0);
        anchoCelda = (int) capa.getTileWidth();
        altoCelda = (int) capa.getTileHeight();
        anchoMapa = capa.getWidth() * anchoCelda;
        altoMapa = capa.getHeight() * altoCelda;

        //Cargamos las capas de los obstáculos.
        TiledMapTileLayer capaObstaculos = (TiledMapTileLayer) mapa.getLayers().get(1);

        //Cargamos la matriz de los obstáculos de los mapas de baldosas.
        capaObstaculos(capaObstaculos);

        //Cargamos en los atributos del ancho y alto del sprite sus valores.
        cuadroActual​​= (TextureRegion) jugador.getKeyFrame(stateTime​​);
        anchoJugador =cuadroActual​​.getRegionWidth();
        altoJugador = cuadroActual​​.getRegionHeight();

        //Inicializamos el apartado referente a los NPC.
        noJugador = new Animation[numeroNPCs];
        noJugadorX = new float[numeroNPCs];
        noJugadorY = new float[numeroNPCs];
        destinoX = new float[numeroNPCs];
        destinoY = new float[numeroNPCs];

        //Creamos las animaciones posicionales de los NPC.
        //Cargamos la imagen de los frames del enemigo en el objeto img de la clase Texture.
        img= new Texture(Gdx.files.internal("magorojo.png"));

        //Sacamos los frames de img en un array de TextureRegion.
        tmp = TextureRegion.split(img, img.getWidth() / FRAME_COLS, img.getHeight() /
                FRAME_ROWS);

        /*
        Creamos las distintas animaciones, teniendo en cuenta que el tiempo de muestra de cada frame
        será de 150 milisegundos.
        */
        noJugadorArriba = new Animation(0.150f, tmp[0]);
        noJugadorArriba.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
        noJugadorDerecha = new Animation(0.150f, tmp[1]);
        noJugadorDerecha.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
        noJugadorAbajo = new Animation(0.150f, tmp[2]);
        noJugadorAbajo.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
        noJugadorIzquierda = new Animation(0.150f, tmp[3]);
        noJugadorIzquierda.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);

        //Cargamos en los atributos del ancho y alto del sprite del monstruo sus valores.
        cuadroActual​​= (TextureRegion) noJugadorAbajo.getKeyFrame(stateTimeNPC);
        anchoNoJugador = cuadroActual​​.getRegionWidth();
        altoNoJugador = cuadroActual​​.getRegionHeight();

        /*
        Se inicializan, la animación por defecto y, de forma aleatoria, las posiciones
        iniciales y finales de los NPC.
        Para simplificar un poco, los NPC pares, se moveran de forma vertical y
        los impares de forma horizontal.
        */
        for (int i = 0; i < numeroNPCs; i++) {
            noJugadorX[i] = (float) (Math.random() * anchoMapa);
            noJugadorY[i] = (float) (Math.random() * altoMapa);

            if (i % 2 == 0) {

                //NPC par => mover de forma vertical.
                destinoX[i] = noJugadorX[i];
                destinoY[i] = (float) (Math.random() * altoMapa);

                //Determinamos cual de las animaciones verticales se utiliza.
                if (noJugadorY[i] < destinoY[i]) {
                    noJugador[i] = noJugadorArriba;
                } else {
                    noJugador[i] = noJugadorAbajo;
                }

            } else {

                //NPC impar => mover de forma horizontal.
                destinoX[i] = (float) (Math.random() * anchoMapa);
                destinoY[i] = noJugadorY[i];

                //Determinamos cual de las animaciones horizontales se utiliza.
                if (noJugadorX[i] < destinoX[i]) {
                    noJugador[i] = noJugadorDerecha;
                } else {
                    noJugador[i] = noJugadorIzquierda;
                }
            }
        }

        //Ponemos a cero el atributo stateTimeNPC, que marca el tiempo e ejecución de la animación de los NPC.
        stateTimeNPC = 0f;

        //Inicializamos la música de fondo del juego y la reproducimos.
        musica = Gdx.audio.newMusic(Gdx.files.internal("Overworld.ogg"));

        //Establecemos que se reproduzca en bucle.
        musica.setLooping(true);
        musica.play();

        //​Inicializamos los atributos de los efectos de sonido.
        sonidoColisionEnemigo = Gdx.audio.newSound(Gdx.files.internal("Punch1.mp3"));
        sonidoPasos = Gdx.audio.newSound(Gdx.files.internal("tap.wav"));
        sonidoObstaculo = Gdx.audio.newSound(Gdx.files.internal("BIGBUTN.WAV"));

	}

	@Override
	public void render () {

        //Ponemos el color del fondo a negro.
        Gdx.gl.glClearColor(0, 0, 0, 1);

        //Borramos la pantalla.
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //Trasladamos la cámara para que se centre en el mosquetero.
        camara.position.set(jugadorX​,jugadorY​​,0f);

        /*
        Comprobamos que la cámara no se salga de los límites del mapa de baldosas.
        Verificamos, con el método clamp, que el valor de la posición x de la cámara
        esté entre la mitad de la anchura de la vista de la cámara y entre la diferencia
        entre la anchura del mapa restando la mitad de la anchura de la vista de la cámara.
        */
        camara.position.x = MathUtils.clamp(camara.position.x, camara.viewportWidth / 2f,
                anchoMapa - camara.viewportWidth / 2f);

        /*
        Verificamos, con el método clamp(), que el valor de la posición y de la cámara esté
        entre la mitad de la altura de la vista de la cámara y entre la diferencia entre
        la altura del mapa restando la mitad de la altura de la vista de la cámara.
        */
        camara.position.y = MathUtils.clamp(camara.position.y, camara.viewportHeight / 2f,
                altoMapa - camara.viewportHeight / 2f);

        //Actualizamos la cámara del juego.
		camara.update();

        //Vinculamos el objeto de dibuja el TiledMap con la cámara del juego.
		mapaRenderer.setView(camara);

        //Dibujamos el TiledMap.
		mapaRenderer.render();

        //​Dibujamos las capas visibles inferiores del TiledMap.
        int[] capas = {0,1,2,3,4,5,6,7,8,9,10,11,12,13};
        mapaRenderer.render(capas);

        //Extraemos el tiempo de la última actualización del sprite y la acumulamos a stateTime.
        stateTime​​ += Gdx.graphics.getDeltaTime();

        //​Extraemos el frame que debe ir asociado al momento actual.
        cuadroActual​​ = (TextureRegion) jugador.getKeyFrame(stateTime​​); // 1

        //Le indicamos al SpriteBatch que se muestre en el sistema de coordenadas específicas de la cámara.
        batch.setProjectionMatrix(camara.combined);

        //Iniciamos el SpriteBatch.
		batch.begin();

		//Actualizamos la posición del jugador.
		actualizaJugador(0.5f);

		//Dibujamos al jugador.
		batch.draw(cuadroActual​​, jugadorX​, jugadorY​​); // 2

        //Dibujamos las animaciones de los NPC.
        for (int i= 0; i < numeroNPCs; i++) {
            actualizaNPC​​​​(i, 0.5f);
            cuadroActual​​ = (TextureRegion) noJugador[i].getKeyFrame(stateTimeNPC);
            batch.draw(cuadroActual​​,noJugadorX[i],noJugadorY[i]);
        }

        //Finalizamos el SpriteBatch.
		batch.end();

        //​Pintamos las capas visibles superiores (superpuestas) del TiledMap.
        capas = new int[2];
        capas[0]= 14;
        capas[1]= 15;
        mapaRenderer.render(capas);

	}

    @Override
    public boolean keyDown(int keycode) {

        /*
        Seteamos la animación para que recorra el sprite del primero al último
        y de vuelta al primero mientras se mueve.
        */
        jugador.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);

        /*
        ​Guardamos la posición anterior del jugador por si al desplazarlo se topa
        con un obstáculo y podamos devolverlo a la posición anterior.
        */
        float jugadorAnteriorX = jugadorX​;
        float jugadorAnteriorY = jugadorY​​;

        if (keycode == Input.Keys.LEFT){
            jugadorX​ -= 5;
            jugador = jugadorIzquierda​​;
        }
        if (keycode == Input.Keys.RIGHT) {
            jugadorX​ += 5;
            jugador = jugadorDerecha​​;
        }
        if (keycode == Input.Keys.UP) {
            jugadorY​​+= 5;
            jugador = jugadorArriba​​;
        }
        if (keycode == Input.Keys.DOWN) {
            jugadorY​​ -= 5;
            jugador = jugadorAbajo​​;
        }

        //Si se pulsa la barra espaciadora...
        if(keycode == Input.Keys.SPACE)
            //Comprobamos si hay o no colisiones entre en jugador y los NPC.
            detectaColisiones​​();

        //​Detectamos las colisiones con los obstáculos del mapa y si el jugador se sale del mismo.
        if((jugadorX​ < 0 || jugadorY​​ < 0 || jugadorX​ > (anchoMapa - anchoJugador) ||
                jugadorY​​ > (altoMapa - altoJugador)) ||
                ((obstaculo [(int) ((jugadorX​ + anchoJugador / 4) / anchoCelda)][((int) (jugadorY​​) / altoCelda)]) ||
                        (obstaculo [(int) ((jugadorX​ + 3 * anchoJugador / 4) / anchoCelda)][((int) (jugadorY​​) / altoCelda)]))){
            jugadorX​ = jugadorAnteriorX;
            jugadorY​​ = jugadorAnteriorY;
            sonidoObstaculo.play();
        }

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {

        //Seteamos la animación para que se pare.
        jugador.setPlayMode(Animation.PlayMode.NORMAL);

        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

	    /*
        Seteamos la animación para que recorra el sprite del primero al último
        y de vuelta al primero mientras se mueve.
        */
        jugador.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);

        //Vector en tres dimensiones que recoge las coordenadas donde se ha hecho click o toque de la pantalla.
        Vector3 clickCoordinates = new Vector3(screenX, screenY, 0);

        //Transformamos las coordenadas del vector a coordenadas de nuestra cámara.
        posicion = camara.unproject(clickCoordinates);

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {

        //Seteamos la animación para que se pare.
        jugador.setPlayMode(Animation.PlayMode.NORMAL);

        //Si se deja de pulsar en la pantalla, la posición se vuelve nula.
        posicion=null;

	    return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {

        //Vector en tres dimensiones que recoge las coordenadas donde se ha hecho click o toque de la pantalla.
        Vector3 clickCoordinates = new Vector3(screenX, screenY, 0);

        //Transformamos las coordenadas del vector a coordenadas de nuestra cámara.
        posicion = camara.unproject(clickCoordinates);

	    return false;

	}

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    @Override
    public void pause() {
        super.pause();
    }

    @Override
    public void resume() {
        super.resume();
    }

    @Override
    public void dispose () {
        mapa.dispose();
        mapaRenderer.dispose();
        img.dispose();
        batch.dispose();
        musica.dispose();
        sonidoObstaculo.dispose();
        sonidoPasos.dispose();
        sonidoColisionEnemigo.dispose();
    }

    /**
     * Método que carga los obstáculos del mapa.
     *
     * @param capa TiledMapTileLayer de la capa de obstáculos.
     */
    private void capaObstaculos (TiledMapTileLayer capa){

        //Cargamos la matriz de los obstáculos del mapa de baldosas.
        int anchoCapa = capa.getWidth(), altoCapa = capa.getHeight();
        obstaculo = new boolean[altoCapa][anchoCapa];
        for (int x = 0; x < anchoCapa; x++) {
            for (int y = 0; y < altoCapa; y++) {
                obstaculo[x][y] = (capa.getCell(x, y) != null);
            }
        }

    }

    /**
     * Método que actualiza la posición del jugador dependiendo de donde se pulse en la pantalla.
     *
     * @param delta Variación delta.
     */
    private void actualizaJugador(float delta){

        //Si la posición no es nula (se está pulsando en la pantalla)...
        if(posicion!=null){

	        /*
        ​Guardamos la posición anterior del jugador por si al desplazarlo se topa
        con un obstáculo y podamos devolverlo a la posición anterior.
        */
            float jugadorAnteriorX = jugadorX​;
            float jugadorAnteriorY = jugadorY​​;

        /*
        Si se ha pulsado por encima de la animación, se sube
        esta 5 píxeles y se reproduce la ​animación del jugador
        desplazándose hacia arriba.
         */
            if ((jugadorY​​ +altoJugador) < posicion.y){
                jugadorY​​ += delta*5;
                jugador = jugadorArriba​​;
        /*​
        Si se ha pulsado por debajo de la animación, se baja esta 5 píxeles y
        se reproduce la animación del jugador desplazándose hacia abajo.
        */
            } else if((jugadorY​​) > posicion.y){
                jugadorY​​ -= delta*5;
                jugador = jugadorAbajo​​;

            }

        /*
        ​Si se ha pulsado más de la mitad del ancho del sprite a la derecha
        de la animación, se mueve esta 5 píxeles a la derecha se reproduce la
        animación del jugador desplazándose hacia la derecha.
        */
            if((jugadorX​ + anchoJugador/2) < posicion.x){
                jugadorX​ += delta*5;
                jugador = jugadorDerecha​​;

        /*
        Si se ha pulsado mas de la mitad del ancho del sprite a la izquierda
        de laanimación, se mueve esta 5 píxeles a la izquierda y se reproduce la
        animación del jugador desplazándose hacia la izquierda.
        */
            } else if ((jugadorX​ - anchoJugador/2) > posicion.x){
                jugadorX​ -= delta*5;
                jugador = jugadorIzquierda​​;

            }

            //​Detectamos las colisiones con los obstáculos del mapa y si el jugador se sale del mismo.
            if((jugadorX​ < 0 || jugadorY​​ < 0 || jugadorX​ > (anchoMapa - anchoJugador) ||
                    jugadorY​​ > (altoMapa - altoJugador)) ||
                    ((obstaculo [(int) ((jugadorX​ + anchoJugador / 4) / anchoCelda)][((int) (jugadorY​​) / altoCelda)]) ||
                            (obstaculo [(int) ((jugadorX​ + 3 * anchoJugador / 4) / anchoCelda)][((int) (jugadorY​​) / altoCelda)]))){
                jugadorX​ = jugadorAnteriorX;
                jugadorY​​ = jugadorAnteriorY;
                sonidoObstaculo.play();
            }

        }

    }

    /**
     * Método que permite cambiar las coordenadas del NPC en la posición "i",
     * dada una variación "delta" en ambas coordenadas.
     *
     * @param i Entero de posición.
     * @param delta Variación delta.
     */
    private void actualizaNPC​​​​(int i, float delta) {

        /*
        ​Guardamos la posición anterior del NPC por si al desplazarlo se topa
        con un obstáculo y podamos devolverlo a la posición anterior.
        */
        float NPCAnteriorX=noJugadorX[i];
        float NPCAnteriorY=noJugadorY[i];

        if (destinoY[i]>noJugadorY[i]) {
            noJugadorY[i] += delta;
            noJugador[i] = noJugadorArriba;
        }
        if (destinoY[i]<noJugadorY[i]) {
            noJugadorY[i] -= delta;
            noJugador[i] = noJugadorAbajo;
        }

        if (destinoX[i]>noJugadorX[i]) {
            noJugadorX[i] += delta;
            noJugador[i] = noJugadorDerecha;
        }
        if (destinoX[i]<noJugadorX[i]) {
            noJugadorX[i] -= delta;
            noJugador[i] = noJugadorIzquierda;
        }

        //​Detectamos las colisiones con los obstáculos del mapa y si el NPC se sale del mismo.
        if((noJugadorX[i] < 0 || noJugadorY[i] < 0 || noJugadorX[i] > (anchoMapa - anchoNoJugador) ||
        noJugadorY[i] > (altoMapa - altoNoJugador)) ||
                ((obstaculo [(int) ((noJugadorX[i] + anchoNoJugador / 4) / anchoCelda)][((int) (noJugadorY[i]) / altoCelda)]) ||
                        (obstaculo [(int) ((noJugadorX[i] + 3 * anchoNoJugador / 4) / anchoCelda)][((int) (noJugadorY[i]) / altoCelda)]))){
            noJugadorX[i] = NPCAnteriorX;
            noJugadorY[i] = NPCAnteriorY;
        }

    }

    /**
     * Método que detecta las colisiones del jugador con los NPC.
     */
    private void detectaColisiones​​() {

        /*
        Vamos a comprobar que el rectángulo que rodea al jugador, no se solape
        con el rectángulo de alguno de los NPC. Primero calculamos el rectángulo
        en torno al jugador.
        */
        Rectangle rJugador=new Rectangle(jugadorX​, jugadorY​​, anchoJugador, altoJugador);

        //Rectángulo del NPC.
        Rectangle rNPC;

        /*
        Ahora recorremos el array de NPC, para cada uno generamos su rectángulo envolvente
        y comprobamos si se solapa o no con el del Jugador.
        */
        for (int i=0; i<numeroNPCs; i++) {
            rNPC = new Rectangle(noJugadorX[i], noJugadorY[i],
                    anchoNoJugador,altoNoJugador);

            //Se comprueba si se solapan.
            if (rJugador.overlaps(rNPC)){

                //Si se solapan, se reproduce un sonido...
                sonidoColisionEnemigo.play();

                //y se desplaza al NPC en dirección contraria a la posición del jugador.
                if(noJugadorX[i]>jugadorX​){
                    noJugadorX[i]+=20;
                } else {
                    noJugadorX[i]-=20;
                }
                if(noJugadorY[i]>jugadorY​​){
                    noJugadorY[i]+=20;
                } else {
                    noJugadorY[i]-=20;
                }

            }
        }
    }
}
