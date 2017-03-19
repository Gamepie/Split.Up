using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using VoxelBusters.Utility;
using VoxelBusters.NativePlugins;
using HutongGames.PlayMaker;


namespace VoxelBusters.NativePlugins {
public class Buyiap : MonoBehaviour {
		public PlayMakerFSM theFSM;
		public PlayMakerFSM buttonFSM;
		public PlayMakerFSM Restoreobj;
		public bool Adsbought = false;

	// Use this for initialization


	void Start () {
			
	}
	
	// Update is called once per frame
	void Update () {

	}
		public void BuyRemoveADS()
		{
			BuyProduct(NPSettings.Billing.Products[0]);
		}

	public void RequestBillingProducts ()
	{
		NPBinding.Billing.RequestForBillingProducts(NPSettings.Billing.Products);

		// At this point you can display an activity indicator to inform user that task is in progress
			Debug.Log ("In progress");
	}



private void OnEnable ()
{
    // Register for callbacks
    Billing.DidFinishRequestForBillingProductsEvent    += OnDidFinishProductsRequest;
    Billing.DidFinishProductPurchaseEvent            += OnDidFinishTransaction;
			Debug.Log ("register callback");
    // For receiving restored transactions.
    Billing.DidFinishRestoringPurchasesEvent        += OnDidFinishRestoringPurchases;

}

private void OnDisable ()
{
    // Deregister for callbacks
    Billing.DidFinishRequestForBillingProductsEvent    -= OnDidFinishProductsRequest;
    Billing.DidFinishProductPurchaseEvent            -= OnDidFinishTransaction;
    Billing.DidFinishRestoringPurchasesEvent        -= OnDidFinishRestoringPurchases;        
}

	private void OnDidFinishProductsRequest (BillingProduct[] _regProductsList, string _error)
	{
		// Hide activity indicator
			Debug.Log ("PreqD");

		// Handle response
		if (_error != null)
		{        
			// Something went wrong
		}
		else 
		{    
			// Inject code to display received products
				Debug.Log ("LNoads");

		}
	}

		public void BuyProduct (BillingProduct _product)
		{
			Debug.Log (_product.Name);
			if (NPBinding.Billing.IsProductPurchased (_product))
			{
				// Show alert message that item is already purchased
				buttonFSM.SendEvent ("Zaeubri");
				Debug.Log ("Bought");
				return;
			}

			// Call method to make purchase
			NPBinding.Billing.BuyProduct(_product);

			// At this point you can display an activity indicator to inform user that task is in progress
			Debug.Log ("Buying...");
		}


		private void OnDidFinishTransaction (BillingTransaction _transaction)
		{
			if (_transaction != null)
			{

				if (_transaction.VerificationState == eBillingTransactionVerificationState.SUCCESS)
				{
					if (_transaction.TransactionState == eBillingTransactionState.PURCHASED)
					{

						Adsbought = true;
						theFSM.FsmVariables.GetFsmBool ("Adsboughtfsm").Value = Adsbought;

					}
				}
			}
		}
		private void RestoreCompletedTransactions ()
		{
			NPBinding.Billing.RestorePurchases ();
		}
		
		private void OnDidFinishRestoringPurchases (BillingTransaction[] _transactions, string _error)
{
			Debug.Log(string.Format("Received restore purchases response. Error = {0}.", _error.GetPrintableString()));
				
			if (_error == null) {                
				foreach (BillingTransaction _eachTransaction in _transactions) {
					Debug.Log("Product Identifier = "         + _eachTransaction.ProductIdentifier);
					Debug.Log("Transaction State = "        + _eachTransaction.TransactionState);
					Debug.Log("Verification State = "        + _eachTransaction.VerificationState);
					Debug.Log("Transaction Date[UTC] = "    + _eachTransaction.TransactionDateUTC);
					Debug.Log("Transaction Date[Local] = "    + _eachTransaction.TransactionDateLocal);
					Debug.Log("Transaction Identifier = "    + _eachTransaction.TransactionIdentifier);
					Debug.Log("Transaction Receipt = "        + _eachTransaction.TransactionReceipt);
					Debug.Log("Error = "                    + _eachTransaction.Error.GetPrintableString());
					if (_eachTransaction.VerificationState == eBillingTransactionVerificationState.SUCCESS) {
						// Insert code to restore product associated with this transaction
						Adsbought = true;
						theFSM.FsmVariables.GetFsmBool ("Adsboughtfsm").Value = Adsbought;
						Restoreobj.SendEvent ("Restored");
					} else if (_eachTransaction.VerificationState == eBillingTransactionVerificationState.FAILED) {
						//something went wrong!
						Restoreobj.SendEvent ("nonrestored");
					} else {
						Restoreobj.SendEvent ("Notvalidated");
						//recieved transaction object isnot validated yet
						//skipping this step will cause unusual behavior
					}
				}
			}
			else 
			{
				Restoreobj.SendEvent ("Didnotworkatall");

				//something went wrong
				//resotre failed

					}

        }
    }
}
	


