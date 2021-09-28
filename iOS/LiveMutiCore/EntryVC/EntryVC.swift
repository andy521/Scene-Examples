import UIKit

public class EntryVC: UIViewController {
    let roomNameTextField = UITextField()
    let button = UIButton()
    public var appId: String!
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        title = "LivePK Entry"
        setup()
        commonInit()
    }
    
    func setup() {
        roomNameTextField.text = "10086"
        button.backgroundColor = .systemBlue
        button.setTitle("Join", for: .normal)
        button.setTitleColor(.white, for: .normal)
        roomNameTextField.placeholder = "room name"
        roomNameTextField.clearButtonMode = .whileEditing
        roomNameTextField.keyboardType = .numberPad
        roomNameTextField.borderStyle = .roundedRect
        
        view.addSubview(roomNameTextField)
        view.addSubview(button)
        
        roomNameTextField.translatesAutoresizingMaskIntoConstraints = false
        button.translatesAutoresizingMaskIntoConstraints = false
        
        roomNameTextField.leftAnchor.constraint(equalTo: view.leftAnchor, constant: 30).isActive = true
        roomNameTextField.rightAnchor.constraint(equalTo: view.rightAnchor, constant: -30).isActive = true
        roomNameTextField.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 10).isActive = true
        
        button.leftAnchor.constraint(equalTo: roomNameTextField.leftAnchor).isActive = true
        button.rightAnchor.constraint(equalTo: roomNameTextField.rightAnchor).isActive = true
        button.topAnchor.constraint(equalTo: roomNameTextField.bottomAnchor, constant: 10).isActive = true
        button.heightAnchor.constraint(equalToConstant: 45).isActive = true
    }
    
    func commonInit() {
        button.addTarget(self, action: #selector(buttonTap(_:)), for: .touchUpInside)
    }
    
    public override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        view.endEditing(true)
    }
    
    @objc func buttonTap(_ sender: Any) {
        view.endEditing(true)
        guard let roomName = roomNameTextField.text, !roomName.isEmpty else {
            return
        }
        
        let vc = MainVC(roomName: roomName, appId: appId)
        let nvc = UINavigationController(rootViewController: vc)
        nvc.modalPresentationStyle = .fullScreen
        present(nvc, animated: true, completion: nil)
    }
}
