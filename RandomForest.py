import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
from sklearn import metrics

# Creating a DataFrame of dataset
data = pd.read_csv('featureExtractionFinal.csv', low_memory=False)
data.head()

data.shape

X=data[['Year', 'Day', 'AmountSent', 'Income', 'Neighbours', 'CoAddresses', 'Successors']]  # Features
y=data['IsRansome']  # Labels

# Split dataset into training set and test set
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.3)

#Create a Classifier
clf=RandomForestClassifier(n_estimators=100)

df.dropna(inplace=True)

#Train the model using the training sets 
clf.fit(X_train,y_train)
y_pred=clf.predict(X_test)

print("Accuracy:",metrics.accuracy_score(y_test, y_pred))


#clf.predict([[2015, 25, 5.714149e+06,1.570000e+07,2,0,1]])

