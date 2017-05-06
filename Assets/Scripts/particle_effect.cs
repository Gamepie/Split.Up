using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class particle_effect : MonoBehaviour {
	ParticleSystem _particle;
	BoxCollider2D _collider;
	//ParticleSystem _System;
	// Use this for initialization
	void Start () {
		
	}
	void Awake() {
		_particle = GetComponentInChildren<ParticleSystem>();
		_collider = GetComponentInChildren<BoxCollider2D> ();
	}
	
	// Update is called once per frame
	void Update () {

	}

	void deActivateCollider () {
		_collider.enabled = false;
	}

	void activatePeffect () {

		_particle.Play ();
	}
	void deactivatePeffect () {
		_particle.Stop ();
	}
}
