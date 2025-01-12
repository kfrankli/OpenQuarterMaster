package tech.ebp.oqm.baseStation.service.barcode;

import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.lib.core.rest.media.CodeImageType;
import uk.org.okapibarcode.backend.Code128;
import uk.org.okapibarcode.backend.HumanReadableLocation;
import uk.org.okapibarcode.backend.QrCode;
import uk.org.okapibarcode.backend.Symbol;
import uk.org.okapibarcode.output.SvgRenderer;

import javax.enterprise.context.ApplicationScoped;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Service to generate barcode images.
 *
 * TODO:: add better labels to images https://github.com/jfree/jfreesvg
 */
@Traced
@ApplicationScoped
public class BarcodeService {
	public static String DATA_MEDIA_TYPE = "image/svg+xml";
	
	private String toImageData(Symbol code){
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		SvgRenderer renderer = new SvgRenderer(os, 1, Color.WHITE, Color.BLACK, true);
		try {
			renderer.render(code);
		} catch(IOException e) {
			throw new IllegalStateException(new RuntimeException(e));
		}
		return os.toString();
	}
	
	public String getBarcodeData(String data){
		Code128 barcode = new Code128();
		barcode.setFontName("Monospaced");
		barcode.setFontSize(16);
		barcode.setModuleWidth(2);
		barcode.setBarHeight(50);
		barcode.setHumanReadableLocation(HumanReadableLocation.BOTTOM);
		barcode.setContent(data);
		
		return toImageData(barcode);
	}
	
	public String getQrCodeData(String data){
		QrCode qrCode = new QrCode();
		qrCode.setContent(data);
		
		return toImageData(qrCode);
	}
	
	public String getCodeData(CodeImageType type, String data){
		switch (type){
			case qrcode:
				return this.getQrCodeData(data);
			case barcode:
				return this.getBarcodeData(data);
		}
		throw new IllegalStateException();
	}
}
