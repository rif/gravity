package com.zedmedia.gravity.credit;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.IQ;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

import com.zedmedia.gravity.R;
import com.zedmedia.gravity.ServerConnection;

public class CreditActivity extends Activity {
	private ServerConnection serverConnection;
	private TextView creditView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_credit);
		serverConnection = ServerConnection.getInstance();
		creditView = (TextView) this.findViewById(R.id.creditText);
		final IQ iq = new IQ() {
			@Override
			public String getChildElementXML() {
				return "<gravity xmlns='custom:iq:gravity'/>";
			}
		};
		iq.setType(IQ.Type.GET);
		PacketCollector collector = serverConnection.getConnection().createPacketCollector(new PacketIDFilter(iq.getPacketID()));
		serverConnection.getConnection().sendPacket(iq);
        IQ response = (IQ) collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
        collector.cancel();
        creditView.setText(response.toXML());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.credit, menu);
		return true;
	}
}
