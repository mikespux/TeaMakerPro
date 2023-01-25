package com.easyway.pos.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;

import com.easyway.pos.R;
import com.easyway.pos.data.DBHelper;
import com.easyway.pos.data.Database;
import com.easyway.pos.synctocloud.RestApiRequest;

/**
 * Created by Michael on 30/06/2016.
 */
public class UserDetailsActivity extends AppCompatActivity {
    public Toolbar toolbar;
    Button btAddUser;
    EditText etFullName, etNewUserId, etPassword;
    Spinner spUserLevel;
    Button btn_svUser;
    DBHelper dbhelper;
    Boolean success = true;
    String s_etFullName, s_etNewUserId, s_etPassword, s_spUserLevel;
    ListView listUsers;
    String accountId;
    TextView textAccountId;
    String user_level, Id;
    ProgressDialog progressDialog;
    SimpleCursorAdapter ca;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);
        setupToolbar();
        initializer();
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_users);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public void initializer() {

        dbhelper = new DBHelper(getApplicationContext());

        btAddUser = findViewById(R.id.btAddUser);
        btAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddUserDialog();
            }
        });
        btAddUser.setVisibility(View.GONE);
        listUsers = this.findViewById(R.id.lvUsers);
        listUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View selectedView, int arg2, long arg3) {
                textAccountId = selectedView.findViewById(R.id.txtAccountId);
                Log.d("Accounts", "Selected Account Id : " + textAccountId.getText().toString());
                showUpdateUserDialog();
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem searchViewItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchViewItem);
        searchView.setQueryHint("Search User ...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
             /*   if(list.contains(query)){
                    adapter.getFilter().filter(query);
                }else{
                    Toast.makeText(MainActivity.this, "No Match found",Toast.LENGTH_LONG).show();
                }*/
                return false;

            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ca.getFilter().filter(newText);
                ca.setFilterQueryProvider(new FilterQueryProvider() {

                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        String user = constraint.toString();

                        return dbhelper.SearchUsername(user);


                    }
                });
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    public void showAddUserDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_user, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Add Users");
        etFullName = dialogView.findViewById(R.id.etFullName);
        etNewUserId = dialogView.findViewById(R.id.etNewUserId);
        etPassword = dialogView.findViewById(R.id.etPassword);
        spUserLevel = dialogView.findViewById(R.id.spUserLevel);


        btn_svUser = dialogView.findViewById(R.id.btn_svUser);
        btn_svUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    s_etFullName = etFullName.getText().toString();
                    s_etNewUserId = etNewUserId.getText().toString();
                    s_etPassword = etPassword.getText().toString();
                    s_spUserLevel = spUserLevel.getSelectedItem().toString();

                    if (s_spUserLevel.equals("Manager")) {

                        user_level = "1";
                    } else {
                        user_level = "2";
                    }

                    if (s_etFullName.equals("") || s_etNewUserId.equals("") || s_etPassword.equals("")) {
                        Toast.makeText(getApplicationContext(), "Please enter All fields", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Cursor checkusername = dbhelper.fetchUsername(s_etNewUserId);
                    //Check for duplicate id number
                    if (checkusername.getCount() > 0) {
                        Toast.makeText(getApplicationContext(), "Username already exists", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dbhelper.AddUsers(s_etFullName, s_etNewUserId, s_etPassword, user_level);
                    if (success) {


                        Toast.makeText(UserDetailsActivity.this, "User Saved successfully!!", Toast.LENGTH_LONG).show();

                        etFullName.setText("");
                        etNewUserId.setText("");
                        etPassword.setText("");
                        spUserLevel.setTag(-1);
                    }
                } catch (Exception e) {
                    success = false;

                    if (success) {
                        Toast.makeText(UserDetailsActivity.this, "Saving  Failed", Toast.LENGTH_LONG).show();
                    }

                }

            }
        });


        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                getdata();

            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                getdata();
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    @SuppressLint("Range")
    public void showUpdateUserDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_user, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Update User");
        accountId = textAccountId.getText().toString();

        etFullName = dialogView.findViewById(R.id.etFullName);
        etNewUserId = dialogView.findViewById(R.id.etNewUserId);
        etPassword = dialogView.findViewById(R.id.etPassword);
        spUserLevel = dialogView.findViewById(R.id.spUserLevel);
        spUserLevel.setEnabled(false);
        dbhelper = new DBHelper(this);
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor account = db.query(Database.OPERATORSMASTER_TABLE_NAME, null,
                " _id = ?", new String[]{accountId}, null, null, null);
        //startManagingCursor(accounts);
        if (account.moveToFirst()) {
            // update view
            etFullName.setText(account.getString(account
                    .getColumnIndex(Database.USERFULLNAME)));
            etNewUserId.setText(account.getString(account
                    .getColumnIndex(Database.USERNAME)));
            etPassword.setText(account.getString(account
                    .getColumnIndex(Database.USERPWD)));


        }
        account.close();
        db.close();
        dbhelper.close();


        btn_svUser = dialogView.findViewById(R.id.btn_svUser);
        btn_svUser.setVisibility(View.GONE);


       /* dialogBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                deleteUser();

            }
        });*/
        dialogBuilder.setNegativeButton("RESET", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                updateUsers();
                getdata();


            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onStart() {
        super.onStart();
        getdata();
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void getdata() {

        try {

            SQLiteDatabase db = dbhelper.getReadableDatabase();
            Cursor accounts = db.query(true, Database.OPERATORSMASTER_TABLE_NAME, null, null, null, null, null, null, null, null);

            String[] from = {Database.ROW_ID, Database.USERNAME, Database.USERFULLNAME};
            int[] to = {R.id.txtAccountId, R.id.txtUserName, R.id.txtUserType};

            ca = new SimpleCursorAdapter(this, R.layout.userlist, accounts, from, to);

            ListView listusers = this.findViewById(R.id.lvUsers);
            listusers.setAdapter(ca);
            dbhelper.close();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void updateUsers() {
        try {
            dbhelper = new DBHelper(this);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            // execute insert command
            progressDialog = ProgressDialog.show(UserDetailsActivity.this,
                    "Resetting Password",
                    "Please Wait.. ");
            Id = new RestApiRequest(getApplicationContext()).resetPin(etNewUserId.getText().toString(), "1234");
            if (Integer.parseInt(Id) > 0) {
                ContentValues values = new ContentValues();
                values.put(Database.USERFULLNAME, etFullName.getText().toString());
                values.put(Database.USERNAME, etNewUserId.getText().toString());
                values.put(Database.USERPWD, "1234");
                s_spUserLevel = spUserLevel.getSelectedItem().toString();
                if (s_spUserLevel.equals("Manager")) {

                    user_level = "1";
                } else {
                    user_level = "2";
                }

                values.put(Database.ACCESSLEVEL, user_level);


                long rows = db.update(Database.OPERATORSMASTER_TABLE_NAME, values,
                        "_id = ?", new String[]{accountId});

                db.close();
                if (rows > 0) {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Resetting Password Successful!",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Sorry! Could not Reset Password!",
                            Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void deleteUser() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this user?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteCurrentAccount();

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    public void deleteCurrentAccount() {
        try {
            DBHelper dbhelper = new DBHelper(this);
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            int rows = db.delete(Database.OPERATORSMASTER_TABLE_NAME, "_id=?", new String[]{accountId});
            dbhelper.close();
            if (rows == 1) {
                Toast.makeText(this, "User Deleted Successfully!", Toast.LENGTH_LONG).show();

                //this.finish();
                getdata();
            } else
                Toast.makeText(this, "Could not delete user!", Toast.LENGTH_LONG).show();

        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }

    }


}
