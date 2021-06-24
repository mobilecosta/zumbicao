package com.example.zumbicao;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import org.andengine.audio.sound.Sound;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.AudioOptions;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.WakeLockOptions;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.JumpModifier;
import org.andengine.entity.scene.CameraScene;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.AutoParallaxBackground;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.andengine.entity.shape.IShape;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.AnimatedSprite.IAnimationListener;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.vbo.ITiledSpriteVertexBufferObject;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.SurfaceGestureDetector;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import android.media.AudioTrack;
import android.os.Bundle;
import android.provider.MediaStore.Audio;
import android.app.Activity;
import android.view.Menu;

public class MainActivity extends SimpleBaseGameActivity {

	private static final int LARGURA = 800;
	private static final int ALTURA = 480;
	private AutoParallaxBackground background;
	private ITextureRegion fundoFixo, fundoMovimento;
	private Scene cena;
	private ITextureRegion regiaoBala;
	private int auto = 5;
	boolean rodando;
	private int tiros = 10;

	private ArrayList<IShape> listaDeColisoes = new ArrayList<IShape>(); 
	
	private TiledTextureRegion regiaoMadrugaCorrendo,
			regiaoMadrugaRecarregando, regiaoMadrugaAtirando,
			regiaoZicaCorrendo, regiaoZumbi;

	private AnimatedSprite madrugaCorrendo, madrugaAtirando,
			madrugaRecarregando, zicaCorrendo, zumbi;

		// TODO Auto-generated method stub

	
	@Override
	public EngineOptions onCreateEngineOptions() {

		final Camera camera = new Camera(0, 0, LARGURA, ALTURA);
//		EngineOptions opcoes = new EngineOptions(true,
//				ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(
//						LARGURA, ALTURA), camera);

		EngineOptions opcoes = new EngineOptions(true,
				ScreenOrientation.LANDSCAPE_FIXED, new FillResolutionPolicy(), camera);
		
		opcoes.getAudioOptions().setNeedsSound(true);
		opcoes.setWakeLockOptions(WakeLockOptions.SCREEN_ON);
		return opcoes;

	}

	@Override
	protected void onCreateResources() {
		mEngine.enableVibrator(this);
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		BitmapTextureAtlas textura = new BitmapTextureAtlas(
				getTextureManager(), 1024, 1024);
		textura.load();

		fundoFixo = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
				textura, this, "background.png", 0, 188);

		textura = new BitmapTextureAtlas(getTextureManager(), 1024, 1024);
		textura.load();

		fundoMovimento = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(textura, this, "trilho.png", 0, 0);

		background = new AutoParallaxBackground(0, 0, 0, 5);

		ParallaxEntity fundo = new ParallaxEntity(0, new Sprite(-1, ALTURA
				- fundoFixo.getHeight(), fundoFixo,
				getVertexBufferObjectManager()));
		background.attachParallaxEntity(fundo);

		ParallaxEntity trilho = new ParallaxEntity(-10, new Sprite(0, ALTURA
				- fundoMovimento.getHeight(), fundoMovimento,
				getVertexBufferObjectManager()));
		background.attachParallaxEntity(trilho);

		textura = new BitmapTextureAtlas(getTextureManager(), 512, 256);
		textura.load();

		regiaoMadrugaCorrendo = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(textura, this, "madruga-correndo.png", 0,
						0, 8, 1);
		madrugaCorrendo = new AnimatedSprite(200, ALTURA
				- regiaoMadrugaCorrendo.getHeight() - 2, regiaoMadrugaCorrendo,
				getVertexBufferObjectManager());
		madrugaCorrendo.setScaleCenterY(madrugaCorrendo.getHeight());
		madrugaCorrendo.setScale(2);
		long[] frames = new long[4];
		Arrays.fill(frames, 100);
		madrugaCorrendo.animate(frames, 0, 3, true);

		/* Terceiro dia - Inicio */
		textura = new BitmapTextureAtlas(this.getTextureManager(), 512, 256,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		textura.load();
		regiaoMadrugaAtirando = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(textura, this, "madruga-atirando.png", 0,
						0, 2, 1);
		madrugaAtirando = new AnimatedSprite(madrugaCorrendo.getX(),
				madrugaCorrendo.getY() + 10, regiaoMadrugaAtirando,
				getVertexBufferObjectManager());
		madrugaAtirando.setScaleCenterY(madrugaAtirando.getHeight());
		madrugaAtirando.setScale(2);

		textura = new BitmapTextureAtlas(this.getTextureManager(), 512, 256,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		textura.load();
		regiaoMadrugaRecarregando = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(textura, this,
						"madruga-recarregando.png", 0, 0, 9, 2);
		madrugaRecarregando = new AnimatedSprite(madrugaCorrendo.getX(),
				madrugaCorrendo.getY() + 10, regiaoMadrugaRecarregando,
				getVertexBufferObjectManager());
		madrugaRecarregando.setScaleCenterY(madrugaRecarregando.getHeight());
		madrugaRecarregando.setScale(2);

		textura = new BitmapTextureAtlas(this.getTextureManager(), 512, 256,
				TextureOptions.BILINEAR);
		textura.load();
		regiaoZicaCorrendo = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(textura, this, "zica-correndo.png", 0, 0,
						8, 1);
		zicaCorrendo = new AnimatedSprite(madrugaCorrendo.getX() - 130,
				madrugaCorrendo.getY() + 10, regiaoZicaCorrendo,
				getVertexBufferObjectManager());
		zicaCorrendo.setScale(1);
		frames = new long[3];
		Arrays.fill(frames, 200);
		zicaCorrendo.animate(frames, 3, 5, true);

		textura = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		textura.load();
		regiaoZumbi = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(textura, this, "zumbis.png", 0, 0, 10, 
						11);

		textura = new BitmapTextureAtlas(this.getTextureManager(), 32, 32,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		textura.load();
		regiaoBala = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
				textura, this, "8-bit-bullet.jpg", 0, 0);
		/* Terceiro dia - FIM */
		
	
	
	}
	//Habilitar a detecçao de gestos,na cena (onfling)
	private void detectarGestos(){
		
		SurfaceGestureDetector surfaceGestureDetector = new SurfaceGestureDetector(this) {
		
			@Override
			protected boolean onSwipeUp() {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						madrugaCorrendo.registerEntityModifier(new JumpModifier(0.5f,madrugaCorrendo.getX(),madrugaCorrendo.getX(),madrugaCorrendo.getY(),madrugaCorrendo.getY(),60));
						}
				});
				
				
				return true;
			}
			
			@Override
			protected boolean onSwipeRight() {
				// TODO Auto-generated method stub
				
				
				return false;
			}
			
			@Override
			protected boolean onSwipeLeft() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			protected boolean onSwipeDown() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			protected boolean onSingleTap() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			protected boolean onDoubleTap() {
				// TODO Auto-generated method stub
				return false;
			}
		};
		
		surfaceGestureDetector.setEnabled(true);
		cena.setOnSceneTouchListener(surfaceGestureDetector);
		
		
	}

	@Override
	protected Scene onCreateScene() {
		cena = new Scene();
		// background.setParallaxChangePerSecond(5);
		cena.setBackground(background);
		cena.attachChild(madrugaCorrendo);
		cena.attachChild(zicaCorrendo);

		final TimerHandler timer = new TimerHandler(3, true,
				new ITimerCallback() {

					@Override
					public void onTimePassed(TimerHandler pTimerHandler) {
						zumbi = new Zumbi(LARGURA - 80, ALTURA
								- regiaoMadrugaCorrendo.getHeight() - 2,
								regiaoZumbi, getVertexBufferObjectManager());
						zumbi.setScaleCenterY(zumbi.getHeight());
						zumbi.setScale(2);

						cena.attachChild(zumbi);
						cena.registerTouchArea(zumbi);
						listaDeColisoes.add(zumbi);
					}
				});

		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				detectarGestos();
				// TODO Auto-generated method stub
				
			}
		});
		timer.setAutoReset(true);
		getEngine().registerUpdateHandler(timer);

		return cena;
	}

	public class Zumbi extends AnimatedSprite {
		PhysicsHandler fisicaHandler;

		public Zumbi(float pX, float pY,
				ITiledTextureRegion pTiledTextureRegion,
				VertexBufferObjectManager vertexBufferObjectManager) {
			super(pX, pY, pTiledTextureRegion, vertexBufferObjectManager);
			fisicaHandler = new PhysicsHandler(this);
			registerUpdateHandler(fisicaHandler);
		}

		@Override
		public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
				float pTouchAreaLocalX, float pTouchAreaLocalY) {
			atirar();
			return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX,
					pTouchAreaLocalY);
		}

		@Override
		public void onAttached() {
		long [] frame = new long[9];
		Arrays.fill(frame, 175); // 100 mile segundos para cada frame, 
			zumbi.animate(frame,30,38,true);// animando os frames, da posi��o x � y , com loop infinito,
			
			
			
			fisicaHandler.setVelocityX(-30);

		}
	}

	public void atirar() {
		if (!rodando) {
			rodando = true;

			// TODO: 1 - Remover de cena o madruga correndo
			cena.detachChild(madrugaCorrendo);

			// TODO: 2 - Adicionar na cena o madruga atirando
			cena.attachChild(madrugaAtirando);

			long[] frames = new long[2];
			Arrays.fill(frames, 200);

			// TODO: 3 - "Animar" o madruga atirando, do frame 0 at? o frame 1,
			// sem loop, e utilizando um Listener para controlar o fluxo da
			// anima??o
			madrugaAtirando.animate(frames, 0, 1, false,
					new IAnimationListener() {

						@Override
						public void onAnimationStarted(
								AnimatedSprite pAnimatedSprite,
								int pInitialLoopCount) {
							// TODO Auto-generated method stub
							rodando = true;
							background.setParallaxChangePerSecond(0);

						}

						@Override
						public void onAnimationLoopFinished(
								AnimatedSprite pAnimatedSprite,
								int pRemainingLoopCount, int pInitialLoopCount) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onAnimationFrameChanged(
								AnimatedSprite pAnimatedSprite,
								int pOldFrameIndex, int pNewFrameIndex) {
							// TODO Auto-generated method stub
							// Verifica se ? o ?ltimo frame
							if (pNewFrameIndex == (pAnimatedSprite
									.getTileCount() - 1)) {
								float y = (ALTURA - madrugaAtirando.getHeight()) - 10;

								final Bala bala = new Bala(madrugaCorrendo
										.getX() + madrugaCorrendo.getWidth(),
										y, regiaoBala,
										getVertexBufferObjectManager());

								cena.attachChild(bala);

								bala.registerUpdateHandler(new IUpdateHandler() {

									@Override
									public void reset() {

									}

									@Override
									public void onUpdate(float pSecondsElapsed) {

										
										for(IShape zumbi : listaDeColisoes){
											
										
										
										if (bala.collidesWith(zumbi)) {
											final Zumbi z = (Zumbi)zumbi;
											
											runOnUpdateThread(new Runnable() {

												@Override
												public void run() {
													// TODO Auto-generated
													// method stub
													cena.unregisterTouchArea(z);
													
													if(bala !=null){
														bala.dispose();
														bala.detachChild(z);
														cena.detachChild(bala);
													}
													
													long[] frames= new long[33];
													Arrays.fill(frames, 170);
													z.animate(frames,63,95,false, new IAnimationListener() {
														
														@Override
														public void onAnimationStarted(AnimatedSprite pAnimatedSprite,
																int pInitialLoopCount) {
															// TODO Auto-generated method stub
															listaDeColisoes.remove(z);
														}
														
														@Override
														public void onAnimationLoopFinished(AnimatedSprite pAnimatedSprite,
																int pRemainingLoopCount, int pInitialLoopCount) {
															// TODO Auto-generated method stub
															
														}
														
														@Override
														public void onAnimationFrameChanged(AnimatedSprite pAnimatedSprite,
																int pOldFrameIndex, int pNewFrameIndex) {
															// TODO Auto-generated method stub
															
														}
														
														@Override
														public void onAnimationFinished(AnimatedSprite pAnimatedSprite) {
															// TODO Auto-generated method stub
															
															runOnUpdateThread(new Runnable() {
																
																@Override
																public void run() {
																	// TODO Auto-generated method stub
																	z.detachSelf();
																}
															}); 
															
															
														}
													});
												}

											});

										}

									}}
								});

							}

						}

						@Override
						public void onAnimationFinished(
								AnimatedSprite pAnimatedSprite) {
							// TODO Auto-generated method stub

							background.setParallaxChangePerSecond(auto);
							--tiros;

							if (tiros == 0) {
								rodando = false;
								recarregar();

							} else {
								cena.detachChild(madrugaAtirando);
								cena.attachChild(madrugaCorrendo);

							}

							rodando = false;

						}
					});

		}
	}

	private void recarregar() {
		if (!rodando) {
			rodando = true;
			cena.detachChild(madrugaAtirando);
			madrugaAtirando.setScaleCenterY(madrugaAtirando.getHeight());
			madrugaAtirando.setScale(2);
			cena.attachChild(madrugaRecarregando);

			long[] frames = new long[9];
			Arrays.fill(frames, 200);

			// TODO: 4 - "Animar" o madruga recarregando, e ap?s finalizar a
			// anima??o, voltar ao madruga correndo.

			madrugaRecarregando.animate(frames, 0, 8, false,
					new IAnimationListener() {

						@Override
						public void onAnimationStarted(
								AnimatedSprite pAnimatedSprite,
								int pInitialLoopCount) {
							// TODO Auto-generated method stub
							background.setParallaxChangePerSecond(0);
							zicaCorrendo.stopAnimation();
						}

						@Override
						public void onAnimationLoopFinished(
								AnimatedSprite pAnimatedSprite,
								int pRemainingLoopCount, int pInitialLoopCount) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onAnimationFrameChanged(
								AnimatedSprite pAnimatedSprite,
								int pOldFrameIndex, int pNewFrameIndex) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onAnimationFinished(
								AnimatedSprite pAnimatedSprite) {
							// TODO Auto-generated method stub
							background.setParallaxChangePerSecond(5);
							long[] frames = new long[3];
							Arrays.fill(frames, 200);
							zicaCorrendo.animate(frames, 3, 5, true);
							cena.detachChild(madrugaAtirando);
							cena.detachChild(madrugaRecarregando);
							cena.attachChild(madrugaCorrendo);

							tiros = 10;
							rodando = false;
						}
					});

		}
	}

	public class Bala extends Sprite implements IEntity {

		PhysicsHandler fisicaHandler;

		public Bala(float pX, float pY, ITextureRegion regiaoBala,
				VertexBufferObjectManager vertexBufferObjectManager) {
			super(pX, pY, regiaoBala, vertexBufferObjectManager);

			fisicaHandler = new PhysicsHandler(this);
			registerUpdateHandler(fisicaHandler);
		}

		@Override
		public void onAttached() {
			fisicaHandler.setVelocityX(500);
		}

	}

}