package com.pmdm.mijuego.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.pmdm.mijuego.MiJuego;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		//Indicamos el título de la ventana del juego.
		config.title = "Mi Juego";
		//Indicamos la anchura de la ventana del juego.
		config.width = 800;
		//Indicamos la altura de la ventana del juego.
		config.height = 480;
		//Lanzamos el juego en su versión de escritorio.
		new LwjglApplication(new MiJuego(), config);
	}
}
