package com.dx.anonymousmessenger;

import android.content.Intent;
import android.os.Bundle;
import android.transition.Explode;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;


public class CreateUserActivity extends AppCompatActivity {
    private String nickname;
    private boolean noBack = false;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void changeToPasswordActivity(){
        showNextFragment(new SetupPasswordFragment());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        Objects.requireNonNull(getSupportActionBar()).hide();
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setExitTransition(new Explode());
        setContentView(R.layout.activity_create_user);
        showNextFragment(new SetupUsernameFragment());
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1){
            switchToMainView();
            finish();
        }
        else {
            if(!noBack){
                super.onBackPressed();
            }
        }
    }

    public void showNextFragment(Fragment f) {
        //if (!getLifecycle().getCurrentState().isAtLeast(STARTED)) return;
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left,android.R.anim.slide_out_right)
                .replace(R.id.fragment_container, f)
                .addToBackStack(null)
                .commit();
    }

    public static void setError(TextInputLayout til, String error,
                                boolean set) {
		if (set) {
			if (til.getError() == null) til.setError(error);
		} else {
			til.setError(null);
		}
	}

    protected void createAccount(String password){
        new Thread(() -> {
            try{
                if(nickname==null || password==null){
                    throw new IllegalStateException();
                }

                ((DxApplication) this.getApplication()).sendNotification("Almost Ready!","Starting tor and warming up to get all we need to connect!",false);

                DxAccount account;
                if(((DxApplication) this.getApplication()).getAccount()==null){
                    account = new DxAccount();
                }else{
                    account = ((DxApplication) this.getApplication()).getAccount();
                }
                account.setNickname(nickname);
                account.setPassword(password);

                ((DxApplication) this.getApplication()).setAccount(account);

                if (!((DxApplication) this.getApplication()).isServerReady()) {
                    if (((DxApplication) this.getApplication()).getTorThread() != null) {
                        ((DxApplication) this.getApplication()).getTorThread().interrupt();
                        ((DxApplication) this.getApplication()).setTorThread(null);
                    }
                    ((DxApplication) this.getApplication()).startTor();
                }

            }catch(Exception e){
                e.printStackTrace();
            }
        }).start();
    }

    public void switchToAppView(){
        Intent intent = new Intent(this, AppActivity.class);
        startActivity(intent);
        finish();
    }

    public void switchToMainView(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}