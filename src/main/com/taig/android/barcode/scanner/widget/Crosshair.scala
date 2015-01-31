package com.taig.android.barcode.scanner.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.View.MeasureSpec

class	Crosshair( attributes: AttributeSet = null, style: Int = 0 )( implicit context: Context )
extends	View( context, attributes, style )
{
	def this( context: Context ) = this()( context )

	def this( context: Context, attributes: AttributeSet ) = this( attributes )( context )

	def this( context: Context, attributes: AttributeSet, style: Int ) = this( attributes, style )( context )

	override def onMeasure( width: Int, height: Int ) =
	{
		val size = math.min( MeasureSpec.getSize( width ), MeasureSpec.getSize( height ) )

		super.onMeasure(
			MeasureSpec.makeMeasureSpec( size, MeasureSpec.EXACTLY ),
			MeasureSpec.makeMeasureSpec( size, MeasureSpec.EXACTLY )
		)
	}
}