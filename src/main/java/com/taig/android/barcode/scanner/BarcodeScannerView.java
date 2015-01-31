package com.taig.android.barcode.scanner;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;

import java.util.*;

public class BarcodeScannerView extends FrameLayout implements Camera.PreviewCallback
{
	public interface ResultHandler
	{
		public void handleResult( Result rawResult );
	}

	public static final List<BarcodeFormat> ALL_FORMATS = new ArrayList<BarcodeFormat>();

	static
	{
		ALL_FORMATS.add( BarcodeFormat.UPC_A );
		ALL_FORMATS.add( BarcodeFormat.UPC_E );
		ALL_FORMATS.add( BarcodeFormat.EAN_13 );
		ALL_FORMATS.add( BarcodeFormat.EAN_8 );
		ALL_FORMATS.add( BarcodeFormat.RSS_14 );
		ALL_FORMATS.add( BarcodeFormat.CODE_39 );
		ALL_FORMATS.add( BarcodeFormat.CODE_93 );
		ALL_FORMATS.add( BarcodeFormat.CODE_128 );
		ALL_FORMATS.add( BarcodeFormat.ITF );
		ALL_FORMATS.add( BarcodeFormat.CODABAR );
		ALL_FORMATS.add( BarcodeFormat.QR_CODE );
		ALL_FORMATS.add( BarcodeFormat.DATA_MATRIX );
		ALL_FORMATS.add( BarcodeFormat.PDF_417 );
	}

	private Camera mCamera;

	private CameraPreview mPreview;

	private View mHud;

	private View mCrosshair;

	private int[] mCrosshairPosition = new int[2];

	private Rect mCrosshairArea = new Rect();

	private MultiFormatReader mMultiFormatReader;

	private List<BarcodeFormat> mFormats;

	private ResultHandler mResultHandler;

	public BarcodeScannerView( Context context )
	{
		this( context, null );
	}

	public BarcodeScannerView( Context context, AttributeSet attributes )
	{
		super( context, attributes );

		mPreview = new CameraPreview( getContext() );
		addView( mPreview );

		TypedArray array = getContext()
			.getTheme()
			.obtainStyledAttributes( attributes, R.styleable.Hud, 0, 0 );

		setHud( array.getResourceId( R.styleable.Hud_hud, R.layout.hud ) );
		array.recycle();

		initMultiFormatReader();
	}

	public View getHud()
	{
		return mHud;
	}

	public void setHud( int layout )
	{
		setHud( LayoutInflater.from( getContext() ).inflate( layout, this, false ) );
	}

	public void setHud( View hud )
	{
		if( mHud != null )
		{
			removeView( mHud );
		}

		mCrosshair = hud.findViewById( R.id.crosshair );
		addView( hud );
	}

	private void initMultiFormatReader()
	{
		Map<DecodeHintType, Object> hints = new EnumMap<DecodeHintType, Object>( DecodeHintType.class );
		hints.put( DecodeHintType.POSSIBLE_FORMATS, getFormats() );
		mMultiFormatReader = new MultiFormatReader();
		mMultiFormatReader.setHints( hints );
	}

	public void startCamera()
	{
		mCamera = CameraUtils.getCameraInstance();

		if( mCamera != null )
		{
			mPreview.setCamera( mCamera, this );
			mPreview.initCameraPreview();
		}
	}

	public void stopCamera()
	{
		if( mCamera != null )
		{
			mPreview.stopCameraPreview();
			mPreview.setCamera( null, null );
			mCamera.release();
			mCamera = null;
		}
	}

	public void setFlash( boolean flag )
	{
		if( mCamera != null && CameraUtils.isFlashSupported( mCamera ) )
		{
			Camera.Parameters parameters = mCamera.getParameters();

			if( flag )
			{
				if( parameters.getFlashMode().equals( Camera.Parameters.FLASH_MODE_TORCH ) )
				{
					return;
				}

				parameters.setFlashMode( Camera.Parameters.FLASH_MODE_TORCH );
			}
			else
			{
				if( parameters.getFlashMode().equals( Camera.Parameters.FLASH_MODE_OFF ) )
				{
					return;
				}

				parameters.setFlashMode( Camera.Parameters.FLASH_MODE_OFF );
			}

			mCamera.setParameters( parameters );
		}
	}

	public boolean getFlash()
	{
		if( mCamera != null && CameraUtils.isFlashSupported( mCamera ) )
		{
			return mCamera
				.getParameters()
				.getFlashMode()
				.equals( Camera.Parameters.FLASH_MODE_TORCH );
		}

		return false;
	}

	public void toggleFlash()
	{
		if( mCamera != null && CameraUtils.isFlashSupported( mCamera ) )
		{
			Camera.Parameters parameters = mCamera.getParameters();

			if( parameters.getFlashMode().equals( Camera.Parameters.FLASH_MODE_TORCH ) )
			{
				parameters.setFlashMode( Camera.Parameters.FLASH_MODE_OFF );
			}
			else
			{
				parameters.setFlashMode( Camera.Parameters.FLASH_MODE_TORCH );
			}

			mCamera.setParameters( parameters );
		}
	}

	public void setAutoFocus( boolean state )
	{
		if( mPreview != null )
		{
			mPreview.setAutoFocus( state );
		}
	}

	public void setFormats( List<BarcodeFormat> formats )
	{
		mFormats = formats;
		initMultiFormatReader();
	}

	public void setResultHandler( ResultHandler resultHandler )
	{
		mResultHandler = resultHandler;
	}

	public Collection<BarcodeFormat> getFormats()
	{
		if( mFormats == null )
		{
			return ALL_FORMATS;
		}

		return mFormats;
	}

	@Override
	public void onPreviewFrame( byte[] data, Camera camera )
	{
		Camera.Parameters parameters = camera.getParameters();
		Camera.Size size = parameters.getPreviewSize();
		int width = size.width;
		int height = size.height;

		if( DisplayUtils.getScreenOrientation( getContext() ) == Configuration.ORIENTATION_PORTRAIT )
		{
			byte[] rotatedData = new byte[data.length];

			for( int y = 0; y < height; y++ )
			{
				for( int x = 0; x < width; x++ )
				{
					rotatedData[x * height + height - y - 1] = data[x + y * width];
				}
			}

			int tmp = width;
			width = height;
			height = tmp;
			data = rotatedData;
		}

		Result rawResult = null;
		PlanarYUVLuminanceSource source = buildLuminanceSource( data, width, height );

		if( source != null )
		{
			BinaryBitmap bitmap = new BinaryBitmap( new HybridBinarizer( source ) );

			try
			{
				rawResult = mMultiFormatReader.decodeWithState( bitmap );
			}
			catch( Exception exception )
			{
				// I've got no idea
			}
			finally
			{
				mMultiFormatReader.reset();
			}
		}

		if( rawResult != null )
		{
			stopCamera();

			if( mResultHandler != null )
			{
				mResultHandler.handleResult( rawResult );
			}
		}
		else
		{
			camera.setOneShotPreviewCallback( this );
		}
	}

	public PlanarYUVLuminanceSource buildLuminanceSource( byte[] data, int width, int height )
	{
		if( mCrosshair == null )
		{
			mCrosshairArea.set( 0, 0, width, height );
		}
		else
		{
			mCrosshair.getLocationOnScreen( mCrosshairPosition );

			// TODO scale up camera preview while maintain proportions
			// TODO migrate to new camera api
			// TODO make this more efficient
			int displayWidth = mPreview.getWidth();
			int displayHeight = mPreview.getHeight();
			float scaleX = width / (float) displayWidth;
			float scaleY = height / (float) displayHeight;

			mCrosshairArea.set(
				(int) ( mCrosshairPosition[0] * scaleX ),
				(int) ( mCrosshairPosition[1] * scaleY ),
				(int) ( ( mCrosshairPosition[0] + mCrosshair.getWidth() ) * scaleX ),
				(int) ( ( mCrosshairPosition[1] + mCrosshair.getHeight() ) * scaleY )
			);
		}

		// Go ahead and assume it's YUV rather than die.
		PlanarYUVLuminanceSource source = null;

		try
		{
			source = new PlanarYUVLuminanceSource(
				data,
				width,
				height,
				mCrosshairArea.left,
				mCrosshairArea.top,
				mCrosshairArea.width(),
				mCrosshairArea.height(),
				false
			);
		}
		catch( Exception e )
		{
			// I've got no idea
		}

		return source;
	}
}
