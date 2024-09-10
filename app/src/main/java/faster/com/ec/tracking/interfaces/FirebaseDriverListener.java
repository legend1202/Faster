package faster.com.ec.tracking.interfaces;


import faster.com.ec.tracking.model.Driver;

public interface FirebaseDriverListener {


    void onDriverAdded(Driver driver);

    void onDriverRemoved(Driver driver);

    void onDriverUpdated(Driver driver);

}
