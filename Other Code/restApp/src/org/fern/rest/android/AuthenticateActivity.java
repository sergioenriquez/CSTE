package org.fern.rest.android;

import org.fern.rest.android.dataObj.DataEventReceiver;
import org.fern.rest.android.dataObj.DataHandler;
import org.fern.rest.android.task.TasksActivity;
import org.fern.rest.android.user.User;
import java.net.URI;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AuthenticateActivity extends Activity {
    private DataHandler handler;
    private Button signInButton;
    private TextView createAccountLink;
    private EditText userUrl;
    private EditText userName;
    private EditText userPassword;
    private ProgressDialog mProgress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authentication_layout);

        mProgress = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
        mProgress.setIndeterminate(true);

        handler = new DataHandler(this, new DataEventReceiver() {
            @Override
            public void onAddUser(User user, CommandResult cr) {
                super.onAddUser(user, cr);
                mProgress.dismiss();
                if (cr == CommandResult.SUCCESS) {
                    Intent intent = new Intent(getApplicationContext(),TasksActivity.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), cr.toString(),Toast.LENGTH_SHORT).show();
                }
            }
        });

        signInButton = (Button) findViewById(R.id.authLogin);
        createAccountLink = (TextView) findViewById(R.id.authCreateLink);
        userUrl = (EditText) findViewById(R.id.authUserLink);
        userName = (EditText) findViewById(R.id.authUserName);
        userPassword = (EditText) findViewById(R.id.authPassword);

        createAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Not yet implemented..", Toast.LENGTH_SHORT).show();
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("unused")
            @Override
            public void onClick(View v) {
                URI url;
                url = URI.create(userUrl.getText().toString());
                String urlCode = url.toString();
                String password = userPassword.getText().toString();
                String username = userName.getText().toString();
                handler.authenticateUser(url, username, password);
                
                if (mProgress != null) {
                    mProgress.setMessage(getString(R.string.userAuthLoading));
                    mProgress.setTitle(R.string.loadingGeneric);
                    mProgress.show();
                }
            }
        });

        //TODO remove this for actual product
        userUrl.setText("http://restapp.dyndns.org:9998/a/Bilbo%20Beutlin");
        userName.setText("Bilbo Beutlin");
        userPassword.setText("obliB");
    }
}
