package com.zedmedia.gravity.credit;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.IQ;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.zedmedia.gravity.R;
import com.zedmedia.gravity.ServerConnection;

public class CreditActivity extends Activity {
	private ServerConnection serverConnection;
	private TextView creditView;
	private Button buyCreditButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_credit);
		serverConnection = ServerConnection.getInstance();
		creditView = (TextView) this.findViewById(R.id.creditText);
		final IQ iq = new IQ() {
			@Override
			public String getChildElementXML() {
				return "<gravity xmlns='custom:iq:gravity:credit'/>";
			}
		};
		iq.setType(IQ.Type.GET);
		PacketCollector collector = serverConnection.getConnection()
				.createPacketCollector(new PacketIDFilter(iq.getPacketID()));
		serverConnection.getConnection().sendPacket(iq);
		IQ response = (IQ) collector.nextResult(SmackConfiguration
				.getPacketReplyTimeout());
		collector.cancel();
		creditView.setText(response.toXML());
		buyCreditButton = (Button) this.findViewById(R.id.buyCredit);
		buyCreditButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final IQ iq = new IQ() {
					@Override
					public String getChildElementXML() {
						return "<gravity xmlns='custom:iq:gravity:credit'>"
								+ 10.0 + "</gravity>";
					}
				};
				iq.setType(IQ.Type.SET);
				serverConnection.getConnection().sendPacket(iq);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.credit, menu);
		return true;
	}
}
