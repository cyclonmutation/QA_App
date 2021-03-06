package jp.techacademy.yoshie.sekiguchi.qa_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class SettingActivity extends AppCompatActivity {

    DatabaseReference mDatabaseReference;
    private EditText mNameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        //Preferenceから表示名を取得してEditTextに反映
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String name = sp.getString(Const.NameKEY, "");
        mNameText = (EditText) findViewById(R.id.nameText);
        mNameText.setText(name);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        //UI初期設定
        setTitle("設定");
        Button changeButton = (Button) findViewById(R.id.changeButton);
        //表示名変更ボタンとログアウトボタンのOnClickListenerを設定
        changeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //Keyboardが出ていたら閉じる
                InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                //Login済user取得
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if(user == null){
                    //未ログインなら何もしない
                    Snackbar.make(v, "ログインしていません", Snackbar.LENGTH_LONG).show();
                    return;
                }

                //ログイン済なら変更した表示名をFirebase保存
                String name = mNameText.getText().toString();
                DatabaseReference userRef = mDatabaseReference.child(Const.UsersPATH).child(user.getUid());
                Map<String, String> data = new HashMap<String, String>();
                data.put("name", name);
                userRef.setValue(data);

                //変更した表示名をPreferenceにも保存
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(Const.NameKEY, name);
                editor.commit();

                Snackbar.make(v, "表示名を変更しました", Snackbar.LENGTH_LONG).show();
            }
        });

        //ログアウトボタンのOnClickListenerでログアウト処理
        Button logoutButton = (Button) findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //ログアウトはFirebaseAuthクラスのsignOut method
                //signOut呼出後はPreferenceに空文字(““)を保存、Snackbarでログアウト完了表示
                FirebaseAuth.getInstance().signOut();
                mNameText.setText("");
                Snackbar.make(v, "ログアウトしました", Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
