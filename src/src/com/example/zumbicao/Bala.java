package com.example.zumbicao;

import org.anddev.andengine.engine.handler.physics.PhysicsHandler;
import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.opengl.vertex.VertexBuffer;

public class Bala extends Sprite implements IEntity{

	
	PhysicsHandler fisicaHandler;
	
	
	public Bala(float pX, float pY,
			ITextureRegion regiaoBala,
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
