package com.example.srijanapp
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.PrecomputedTextCompat
import androidx.core.widget.TextViewCompat
import com.example.srijanapp.databinding.ActivityMainBinding
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var usernamefield: EditText
    lateinit var passwordfield: EditText
    lateinit var usererrorbox: TextView
    lateinit var passerrorbox: TextView
    lateinit var loginbuttion: Button
    var cnt2=0;

    @FlowPreview
    @ExperimentalCoroutinesApi
    @DelicateCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usernamefield = findViewById(R.id.editText)
        passwordfield = findViewById(R.id.editText2)
        usererrorbox = findViewById(R.id.username_error)
        passerrorbox = findViewById(R.id.password_error)
        loginbuttion = findViewById(R.id.button)
        var cnt1=0;

        //Respond to text change events in enterEmail//
        RxTextView.afterTextChangeEvents(usernamefield) //Skip enterEmail’s initial, empty state//
            .skipInitialValue()
            .map{
                usererrorbox.error = null
                it.view().text.toString()
            }.debounce(4, //Make sure we’re in Android’s main UI thread//
                TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread())
           .compose(validateEmailAddress)
            .compose(retryWhenError {
                usererrorbox.visibility= View.VISIBLE
                usererrorbox.text = it.message.toString()
                loginbuttion.setBackgroundColor(Color.GRAY)
                cnt2=0
            })
            .subscribe({
                usererrorbox.visibility= View.VISIBLE
                usererrorbox.text="good user"
                cnt1=1;
            })

       RxTextView.afterTextChangeEvents(passwordfield)
            .skipInitialValue()
            .map {
                passerrorbox.error = null
                it.view().text.toString()
            }
            .debounce(4, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread())
            .compose(validatePassword)
            .compose(retryWhenError {
                passerrorbox.visibility= View.VISIBLE
                passerrorbox.text = it.message.toString()
                cnt2=0
                loginbuttion.setBackgroundColor(Color.GRAY)
            })
           .subscribe({
               passerrorbox.visibility= View.VISIBLE
               passerrorbox.text="good pass"
               if(cnt1==1) {
                   loginbuttion.setBackgroundColor(Color.BLUE)
                   cnt2=1
               }
           })
    }


    //If the app encounters an error, then try again//
    private inline fun retryWhenError(crossinline onError: (ex: Throwable) -> Unit): ObservableTransformer<String, String> = ObservableTransformer { observable ->
        observable.retryWhen { errors ->
            ///Use the flatmap() operator to flatten all emissions into a single Observable//
            errors.flatMap {
                onError(it)
                Observable.just("")
            }
        }
    }

//Define our ObservableTransformer and specify that the input and output must be a string//
    private val validatePassword = ObservableTransformer<String, String> { observable ->
        observable.flatMap {
            Observable.just(it).map { it.trim() }
//Check that the password is at least 7 characters long//
                .filter { it.length > 7 } //If the password is less than 7 characters, then throw an error//
                .singleOrError() //If an error occurs, then display the following message//
                .onErrorResumeNext {
                    if (it is NoSuchElementException) {
                        Single.error(Exception("Your password must be 7 characters or more"))
                    } else {
                        Single.error(it)
                    }
                }
                .toObservable()
        }
    }

//Define an ObservableTransformer, where we’ll perform the email validation//

    private val validateEmailAddress = ObservableTransformer<String, String> { observable ->
        observable.flatMap {
            Observable.just(it).map { it.trim() } //Check whether the user input matches Android’s email pattern//
                .filter {
                    Patterns.EMAIL_ADDRESS.matcher(it).matches()
                } //If the user’s input doesn’t match the email pattern, then throw an error//
                .singleOrError()
                .onErrorResumeNext {
                    if (it is NoSuchElementException) {
                        Single.error(Exception("Please enter a valid email address"))
                    } else {
                        Single.error(it)
                    }
                }
                .toObservable()
        }
    }

    fun Signin(view: android.view.View) {

    }
    fun login(view: android.view.View) {
        if(cnt2==1) {
            startActivity(Intent(this, home::class.java).putExtra("text",usernamefield.text.toString()))
        }
    }
}

